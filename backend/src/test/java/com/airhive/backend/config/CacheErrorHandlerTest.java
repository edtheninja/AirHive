
package com.airhive.backend.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

class CacheErrorHandlerTest {

    private CacheErrorHandler errorHandler;
    private Cache cache;

    @BeforeEach
    void setUp() {
        errorHandler = new CacheConfig().errorHandler();
        cache = mock(Cache.class);
        when(cache.getName()).thenReturn("airports");
    }

    @Test
    void cacheGetFailure_shouldAllowUnderlyingServiceFallback() {
        RuntimeException exception =
                new RuntimeException("Redis unavailable");

        assertDoesNotThrow(() ->
                errorHandler.handleCacheGetError(exception, cache, 1L));

        verify(cache).getName();
    }

    @Test
    void cachePutFailure_shouldNotDiscardUnderlyingServiceResult() {
        RuntimeException exception =
                new RuntimeException("Redis unavailable");

        assertDoesNotThrow(() ->
                errorHandler.handleCachePutError(
                        exception, cache, 1L, "airport"));

        verify(cache).getName();
    }

    @Test
    void cacheEvictionFailure_shouldBePropagated() {
        RuntimeException exception =
                new RuntimeException("Redis unavailable");

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> errorHandler.handleCacheEvictError(
                        exception, cache, 1L));

        org.junit.jupiter.api.Assertions.assertSame(exception, thrown);
        verify(cache).getName();
    }

    @Test
    void cacheClearFailure_shouldBePropagated() {
        RuntimeException exception =
                new RuntimeException("Redis unavailable");

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> errorHandler.handleCacheClearError(
                        exception, cache));

        org.junit.jupiter.api.Assertions.assertSame(exception, thrown);
        verify(cache).getName();
    }
}
