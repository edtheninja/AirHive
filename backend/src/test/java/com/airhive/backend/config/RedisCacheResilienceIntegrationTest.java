package com.airhive.backend.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
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
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.ToxiproxyContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import com.airhive.backend.entity.Airport;
import com.airhive.backend.repository.AirportRepository;
import com.airhive.backend.repository.FlightRepository;
import com.airhive.backend.repository.RouteRepository;
import com.airhive.backend.service.AirportService;

import io.lettuce.core.ClientOptions;

@SpringJUnitConfig(classes = {
        CacheConfig.class,
        RedisCacheResilienceIntegrationTest.TestConfig.class
})
class RedisCacheResilienceIntegrationTest {

    // Must match the key used by AirportService.getAllAirports().
    // The log shows "Cache read failed for 'airports' and key 'all'", so the
    // service uses key = "'all'" and NOT SimpleKey.EMPTY.
    private static final String ALL_AIRPORTS_KEY = "all";

    private static final Network NETWORK = Network.newNetwork();

    private static final GenericContainer<?> REDIS = new GenericContainer<>("redis:8-alpine")
            .withNetwork(NETWORK)
            .withNetworkAliases("redis-cache-test")
            .withExposedPorts(6379)
            // Docker Desktop can accept forwarded connections before Redis is
            // actually ready, which shows up as "Connection reset" in Lettuce.
            .waitingFor(Wait.forLogMessage(".*Ready to accept connections.*", 1));

    private static final ToxiproxyContainer TOXIPROXY = new ToxiproxyContainer("ghcr.io/shopify/toxiproxy:2.5.0")
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
            RedisStandaloneConfiguration server = new RedisStandaloneConfiguration(
                    TOXIPROXY.getHost(),
                    REDIS_PROXY.getProxyPort());

            LettuceClientConfiguration client = LettuceClientConfiguration.builder()
                    .clientOptions(ClientOptions.builder()
                            // Fail fast while disconnected instead of queueing
                            // commands that Lettuce would replay after recovery
                            // (a replayed SET could re-populate the cache after
                            // the test has cleared it).
                            .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                            .build())
                    .commandTimeout(Duration.ofSeconds(2))
                    .build();

            LettuceConnectionFactory factory = new LettuceConnectionFactory(server, client);
            factory.afterPropertiesSet();
            return factory;
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

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @BeforeEach
    void clearTestCache() {
        Cache cache = cacheManager.getCache("airports");
        assertNotNull(cache);
        cache.clear();
        assertNull(cache.get(ALL_AIRPORTS_KEY));
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

        Cache cache = cacheManager.getCache("airports");
        assertNotNull(cache);

        // Redis is reachable, but the cache is empty.
        // The first call should query the underlying service.
        assertEquals(1, airportService.getAllAirports().size());

        // Confirm the result really is stored under the key we assert on.
        assertNotNull(cache.get(ALL_AIRPORTS_KEY),
                "Entry should be cached under key '" + ALL_AIRPORTS_KEY
                        + "'; check the key in AirportService.getAllAirports()");

        // Confirm a second call is served from Redis.
        assertEquals(1, airportService.getAllAirports().size());
        verify(airportRepository, times(1)).findAll();

        // Clear the cache while Redis is healthy, then inject an outage.
        cache.clear();
        assertNull(cache.get(ALL_AIRPORTS_KEY));

        REDIS_PROXY.setConnectionCut(true);

        try {
            // A failed cache read and cache write should not discard
            // the result obtained from the underlying service.
            assertEquals(1, airportService.getAllAirports().size());
            verify(airportRepository, times(2)).findAll();
        } finally {
            REDIS_PROXY.setConnectionCut(false);
        }

        // Wait until Redis answers again before touching the cache.
        waitForRedis();

        // Clear and verify the entry is really gone (real key, so this is a
        // meaningful check rather than a vacuous one).
        cache.clear();
        assertNull(cache.get(ALL_AIRPORTS_KEY),
                "Cache entry reappeared after clear(); a write from the outage "
                        + "was probably replayed after recovery");

        // The first request should query the repository and repopulate
        // the cache. The second request should use the cached result.
        assertEquals(1, airportService.getAllAirports().size());
        verify(airportRepository, times(3)).findAll();

        assertEquals(1, airportService.getAllAirports().size());
        verify(airportRepository, times(3)).findAll();
    }

    private void waitForRedis() throws InterruptedException {
        long deadline = System.nanoTime() + Duration.ofSeconds(15).toNanos();
        RuntimeException last = null;

        while (System.nanoTime() < deadline) {
            try (RedisConnection connection = redisConnectionFactory.getConnection()) {
                if ("PONG".equals(connection.ping())) {
                    return;
                }
            } catch (RuntimeException exception) {
                last = exception;
            }
            Thread.sleep(100);
        }

        fail("Redis did not become reachable again within 15 seconds: " + last);
    }
}