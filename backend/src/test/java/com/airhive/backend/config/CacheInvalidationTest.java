package com.airhive.backend.config;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.springframework.cache.annotation.CacheEvict;

import com.airhive.backend.service.AircraftService;
import com.airhive.backend.service.AircraftTypeService;
import com.airhive.backend.service.AirportService;
import com.airhive.backend.service.FlightService;

class CacheInvalidationTest {

    @Test
    void airportMutations_shouldEvictAirportsAndFlights() {
        assertEvicts(
                AirportService.class,
                "createAirport",
                "airports",
                "flights"
        );

        assertEvicts(
                AirportService.class,
                "updateAirport",
                "airports",
                "flights"
        );

        assertEvicts(
                AirportService.class,
                "deleteAirport",
                "airports",
                "flights"
        );
    }

    @Test
    void aircraftMutations_shouldEvictAircraftAndFlights() {
        assertEvicts(
                AircraftService.class,
                "createAircraft",
                "aircraft",
                "flights"
        );

        assertEvicts(
                AircraftService.class,
                "updateAircraft",
                "aircraft",
                "flights"
        );

        assertEvicts(
                AircraftService.class,
                "deleteAircraft",
                "aircraft",
                "flights"
        );
    }

    @Test
    void aircraftTypeMutations_shouldEvictAircraftTypesAndAircraft() {
        assertEvicts(
                AircraftTypeService.class,
                "createAircraftType",
                "aircraftTypes",
                "aircraft"
        );

        assertEvicts(
                AircraftTypeService.class,
                "updateAircraftType",
                "aircraftTypes",
                "aircraft"
        );

        assertEvicts(
                AircraftTypeService.class,
                "deleteAircraftType",
                "aircraftTypes",
                "aircraft"
        );
    }

    @Test
    void flightMutations_shouldEvictFlights() {
        assertEvicts(
                FlightService.class,
                "createFlight",
                "flights"
        );

        assertEvicts(
                FlightService.class,
                "updateFlight",
                "flights"
        );

        assertEvicts(
                FlightService.class,
                "deleteFlight",
                "flights"
        );
    }

    private void assertEvicts(
            Class<?> serviceClass,
            String methodName,
            String... expectedCaches) {

        Method method = findMethod(serviceClass, methodName);

        CacheEvict cacheEvict = method.getAnnotation(CacheEvict.class);

        assertEquals(
                1,
                cacheEvict.allEntries()
                        ? 1
                        : 0,
                serviceClass.getSimpleName() + "." + methodName
                        + " must evict all cache entries"
        );

        assertArrayEquals(
                expectedCaches,
                cacheEvict.value(),
                serviceClass.getSimpleName() + "." + methodName
                        + " cache targets"
        );
    }

    private Method findMethod(Class<?> serviceClass, String methodName) {
        return java.util.Arrays.stream(serviceClass.getDeclaredMethods())
                .filter(method -> method.getName().equals(methodName))
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError(
                                "Method not found: "
                                        + serviceClass.getSimpleName()
                                        + "." + methodName));
    }
}
