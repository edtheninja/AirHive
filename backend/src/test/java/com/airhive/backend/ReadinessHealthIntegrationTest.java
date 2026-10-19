
package com.airhive.backend;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Duration;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.actuate.endpoint.CompositeHealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.ToxiproxyContainer;

@SpringBootTest(properties = {
        "management.endpoint.health.probes.enabled=true",
        "management.endpoint.health.group.readiness.include=readinessState,db"
})
class ReadinessHealthIntegrationTest {

    private static final Duration READINESS_TIMEOUT = Duration.ofSeconds(20);

    private static final Network DATABASE_NETWORK = Network.newNetwork();

    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:18.6")
                    .withDatabaseName("airhive")
                    .withUsername("airhive_user")
                    .withPassword("airhive_dev")
                    .withNetwork(DATABASE_NETWORK)
                    .withNetworkAliases("airhive-postgres");

    private static final ToxiproxyContainer TOXIPROXY =
            new ToxiproxyContainer("ghcr.io/shopify/toxiproxy:2.5.0")
                    .withNetwork(DATABASE_NETWORK);

    private static final ToxiproxyContainer.ContainerProxy databaseProxy;

    static {
        POSTGRES.start();

        try {
            TOXIPROXY.start();
            databaseProxy = TOXIPROXY.getProxy(POSTGRES, 5432);
        } catch (RuntimeException exception) {
            try {
                TOXIPROXY.stop();
            } finally {
                try {
                    POSTGRES.stop();
                } finally {
                    DATABASE_NETWORK.close();
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
                POSTGRES.stop();
            } finally {
                DATABASE_NETWORK.close();
            }
        }
    }

    @Autowired
    private HealthEndpoint healthEndpoint;

    @Autowired
    private DataSource dataSource;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () ->
                jdbcUrl());

        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.datasource.hikari.connection-timeout", () -> 3000);
        registry.add("spring.datasource.hikari.validation-timeout", () -> 1000);
    }

    @Test
    void readinessTracksDatabaseOutageAndRecovery() throws Exception {
        assertDatabaseConnection();
        assertStatus("readiness", "UP", READINESS_TIMEOUT);
        assertDatabaseComponentStatus("UP");

        System.out.println("Verified baseline: readiness UP, database UP.");

        try {
            databaseProxy.setConnectionCut(true);

            assertStatus("readiness", "DOWN", READINESS_TIMEOUT);
            assertStatus("liveness", "UP", Duration.ofSeconds(10));
            assertDatabaseComponentStatus("DOWN");

            System.out.println(
                    "Verified outage: readiness DOWN, liveness UP, database DOWN.");
        } finally {
            databaseProxy.setConnectionCut(false);
        }

        if (dataSource instanceof HikariDataSource hikariDataSource) {
            hikariDataSource.getHikariPoolMXBean().softEvictConnections();
        }

        awaitDatabaseConnection(READINESS_TIMEOUT);

        assertDatabaseConnection();
        assertStatus("readiness", "UP", READINESS_TIMEOUT);
        assertDatabaseComponentStatus("UP");

        System.out.println("Verified recovery: readiness UP, database UP.");
    }

    private void assertDatabaseConnection() throws Exception {
        try (Connection connection = dataSource.getConnection();
                var statement = connection.createStatement();
                var result = statement.executeQuery("SELECT 1")) {

            if (!result.next() || result.getInt(1) != 1) {
                throw new AssertionError(
                        "PostgreSQL SELECT 1 returned an unexpected result.");
            }
        }
    }

    private void awaitDatabaseConnection(Duration timeout)
            throws InterruptedException {

        long deadline = System.nanoTime() + timeout.toNanos();
        Exception lastFailure = null;

        while (System.nanoTime() < deadline) {
            try (Connection connection = DriverManager.getConnection(
                    jdbcUrl(),
                    POSTGRES.getUsername(),
                    POSTGRES.getPassword());
                    var statement = connection.createStatement();
                    var result = statement.executeQuery("SELECT 1")) {

                if (result.next() && result.getInt(1) == 1) {
                    return;
                }
            } catch (Exception exception) {
                lastFailure = exception;
            }

            Thread.sleep(250);
        }

        throw new AssertionError(
                "PostgreSQL did not recover through Toxiproxy within " + timeout,
                lastFailure);
    }

    private static String jdbcUrl() {
        return "jdbc:postgresql://"
                + TOXIPROXY.getHost()
                + ":"
                + databaseProxy.getProxyPort()
                + "/"
                + POSTGRES.getDatabaseName()
                + "?connectTimeout=2&socketTimeout=2";
    }

    private void assertDatabaseComponentStatus(String expected) {
        HealthDescriptor health = healthEndpoint.healthForPath("readiness");

        assertNotNull(health, "Readiness health group must exist.");

        if (!(health instanceof CompositeHealthDescriptor composite)) {
            throw new AssertionError(
                    "Readiness health group did not expose components.");
        }

        HealthDescriptor database = composite.getComponents().get("db");

        assertNotNull(
                database,
                "Readiness health group must contain the db component.");

        String actual = database.getStatus().getCode();

        if (!expected.equals(actual)) {
            throw new AssertionError(
                    "Expected database health " + expected + " but got " + actual);
        }
    }

    private void assertStatus(
            String group,
            String expected,
            Duration timeout) throws InterruptedException {

        long deadline = System.nanoTime() + timeout.toNanos();
        String lastStatus = "not available";

        while (System.nanoTime() < deadline) {
            HealthDescriptor health = healthEndpoint.healthForPath(group);

            assertNotNull(health, "Health group must exist: " + group);

            lastStatus = health.getStatus().getCode();

            if (expected.equals(lastStatus)) {
                System.out.println(
                        "Health group '" + group + "' reached " + lastStatus);
                return;
            }

            Thread.sleep(250);
        }

        throw new AssertionError(
                "Health group '" + group + "' did not reach " + expected
                        + " within " + timeout
                        + ". Last status: " + lastStatus);
    }
}
