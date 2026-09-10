package com.airhive.backend.config;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.airhive.backend.entity.Airport;
import com.airhive.backend.repository.AirportRepository;
import com.airhive.backend.service.AirportService;

@SpringJUnitConfig(classes = {
        CacheConfigTest.TestConfig.class
})
class CacheConfigTest {

    @Configuration
    @EnableCaching(proxyTargetClass = true)
    static class TestConfig {

        @Bean
        RedisConnectionFactory redisConnectionFactory() {
            return new LettuceConnectionFactory("localhost", 6379);
        }

        @Bean
        CacheManager cacheManager(
                RedisConnectionFactory redisConnectionFactory) {

            return new CacheConfig()
                    .cacheManager(redisConnectionFactory);
        }

        @Bean
        AirportRepository airportRepository() {
            return mock(AirportRepository.class);
        }

        @Bean
        AirportService airportService(
                AirportRepository airportRepository) {

            return new AirportService(airportRepository);
        }
    }

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private AirportService airportService;

    @Autowired
    private AirportRepository airportRepository;

    @BeforeEach
    void clearCache() {
        Cache cache = cacheManager.getCache("airports");

        if (cache != null) {
            cache.clear();
        }
    }

    @AfterEach
    void cleanupCache() {
        Cache cache = cacheManager.getCache("airports");

        if (cache != null) {
            cache.clear();
        }
    }

    @Test
    void cacheManager_shouldUseRedis() {
        assertNotNull(cacheManager);
        assertTrue(cacheManager instanceof RedisCacheManager);

        assertNotNull(cacheManager.getCache("airports"));
        assertNotNull(cacheManager.getCache("aircraft"));
        assertNotNull(cacheManager.getCache("aircraftTypes"));
        assertNotNull(cacheManager.getCache("flights"));
    }

    @Test
    void getAllAirports_shouldReturnCachedResultOnSecondCall() {
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

        List<?> firstResult = airportService.getAllAirports();
        List<?> secondResult = airportService.getAllAirports();

        assertEquals(1, firstResult.size());
        assertEquals(1, secondResult.size());

        verify(airportRepository, times(1)).findAll();
    }
}