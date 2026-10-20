package com.airhive.backend.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.ToxiproxyContainer;


import com.airhive.backend.entity.Airport;
import com.airhive.backend.repository.AirportRepository;
import com.airhive.backend.repository.FlightRepository;
import com.airhive.backend.repository.RouteRepository;
import com.airhive.backend.service.AirportService;

@SpringJUnitConfig(classes = {
        CacheConfig.class,
        RedisCacheResilienceIntegrationTest.TestConfig.class
})
class RedisCacheResilienceIntegrationTest {

    private static final Network NETWORK = Network.newNetwork();

    private static final GenericContainer<?> REDIS =
            new GenericContainer<>("redis:8-alpine")
                    .withNetwork(NETWORK)
                    .withNetworkAliases("redis-cache-test")
                    .withExposedPorts(6379);

    private static final ToxiproxyContainer TOXIPROXY =
            new ToxiproxyContainer("ghcr.io/shopify/toxiproxy:2.5.0")
                    .withNetwork(NETWORK);

    private static final ToxiproxyContainer.ContainerProxy REDIS_PROXY;

    static {
        REDIS.start();

        try {
            TOXIPROXY.start();
            REDIS_PROXY = TOXIPROXY.getProxy(REDIS, 6379);
        } catch (RuntimeException exception) {
            try {
                TOXIPROXY.stop();
            } finally {
                try {
                    REDIS.stop();
                } finally {
                    NETWORK.close();
                }
            }
            throw exception;
        }
    }

    @AfterAll
    static void stopContainers() {
        try {
            TOXIPROXY.stop();
        } finally {
            try {
                REDIS.stop();
            } finally {
                NETWORK.close();
            }
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class TestConfig {

        @Bean
        RedisConnectionFactory redisConnectionFactory() {
            RedisStandaloneConfiguration server =
                    new RedisStandaloneConfiguration(
                            TOXIPROXY.getHost(),
                            REDIS_PROXY.getProxyPort());

            LettuceClientConfiguration client =
                    LettuceClientConfiguration.builder()
                            .commandTimeout(Duration.ofSeconds(3))
                            .build();

            return new LettuceConnectionFactory(server, client);
        }

        @Bean
        AirportRepository airportRepository() {
            return mock(AirportRepository.class);
        }

        @Bean
        FlightRepository flightRepository() {
            return mock(FlightRepository.class);
        }

        @Bean
        RouteRepository routeRepository() {
            return mock(RouteRepository.class);
        }

        @Bean
        AirportService airportService(
                AirportRepository airportRepository,
                FlightRepository flightRepository,
                RouteRepository routeRepository) {
            return new AirportService(
                    airportRepository,
                    flightRepository,
                    routeRepository);
        }
    }

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private AirportService airportService;

    @Autowired
    private AirportRepository airportRepository;

    @BeforeEach
    void clearTestCache() {
        Cache cache = cacheManager.getCache("airports");
        assertNotNull(cache);
        cache.clear();
    }

    @Test
    void cachedAirportReadShouldSurviveRedisOutageAndRecover()
            throws Exception {

        Airport airport = new Airport();
        airport.setId(1L);
        airport.setIataCode("DEL");
        airport.setIcaoCode("VIDP");
        airport.setName("Indira Gandhi International Airport");
        airport.setCity("Delhi");
        airport.setCountry("India");
        airport.setTerminalCount(3);
        airport.setStatus("ACTIVE");

        when(airportRepository.findAll())
                .thenReturn(List.of(airport));

        // Redis is reachable, but the cache is empty.
        // The first call should query the underlying service.
        assertEquals(1, airportService.getAllAirports().size());

        // Confirm a second call is served from Redis.
        assertEquals(1, airportService.getAllAirports().size());
        verify(airportRepository, times(1)).findAll();

        // Clear the cache while Redis is healthy, then inject an outage.
        Cache cache = cacheManager.getCache("airports");
        assertNotNull(cache);
        cache.clear();

        REDIS_PROXY.setConnectionCut(true);

        try {
            // A failed cache read and cache write should not discard
            // the result obtained from the underlying service.
            assertEquals(1, airportService.getAllAirports().size());
            verify(airportRepository, times(2)).findAll();
                } finally {
            REDIS_PROXY.setConnectionCut(false);
        }

        // Redis is reachable again. Start recovery verification with an
        // empty cache so the next request must query the underlying service.
        cache.clear();

        // The first request repopulates the cache.
        assertEquals(1, airportService.getAllAirports().size());

        assertEquals(1, airportService.getAllAirports().size());

        verify(airportRepository, times(3)).findAll();
    }
}
