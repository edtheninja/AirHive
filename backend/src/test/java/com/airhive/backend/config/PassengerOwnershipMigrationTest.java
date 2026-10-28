package com.airhive.backend.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Exercises the V1 -> V2 upgrade path with representative pre-existing data.
 * Existing records remain intact and unassigned until an explicit ownership
 * reconciliation is performed.
 */
@Testcontainers
class PassengerOwnershipMigrationTest {

    private static final String LEGACY_USERNAME = "legacy-viewer-migration-test";

    @Container
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:18.6");

    static {
        POSTGRES.start();

        Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .target(MigrationVersion.fromVersion("1"))
                .load()
                .migrate();

        try (Connection connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
                Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    INSERT INTO app_users (enabled, password, role, username)
                    VALUES (TRUE, 'test-hash', 'VIEWER', 'legacy-viewer-migration-test')
                    """);
            statement.executeUpdate("""
                    INSERT INTO bookings
                    (amount, cabin, flight, passenger, pnr, route, seat, status, travel_date)
                    VALUES (1499.50, 'ECONOMY', 'AH101', 'Legacy Passenger',
                            'LEGACY-MIGRATION-PNR', 'DEL-BOM', '12A', 'CONFIRMED', DATE '2026-11-15')
                    """);
            statement.executeUpdate("""
                    INSERT INTO passengers
                    (bags, checked_in, flight, name, passenger_code, route, seat, tier)
                    VALUES (1, FALSE, 'AH101', 'Legacy Passenger', 'LEGACY-MIGRATION-PAX',
                            'DEL-BOM', '12A', 'SILVER')
                    """);
        } catch (Exception exception) {
            throw new ExceptionInInitializerError(exception);
        }

        Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }

    @Test
    void migrationReclassifiesLegacyViewerAndPreservesUnassignedRecords() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT role FROM app_users WHERE username = ?")) {
                statement.setString(1, LEGACY_USERNAME);
                try (ResultSet result = statement.executeQuery()) {
                    result.next();
                    assertEquals("PASSENGER", result.getString("role"));
                }
            }

            try (Statement statement = connection.createStatement();
                    ResultSet result = statement.executeQuery("""
                            SELECT b.user_id,
                                   (SELECT COUNT(*) FROM bookings WHERE pnr = 'LEGACY-MIGRATION-PNR') AS booking_count,
                                   (SELECT COUNT(*) FROM passengers WHERE passenger_code = 'LEGACY-MIGRATION-PAX') AS passenger_count
                            FROM bookings b
                            WHERE b.pnr = 'LEGACY-MIGRATION-PNR'
                            """)) {
                result.next();
                assertNull(result.getObject("user_id"));
                assertEquals(1, result.getInt("booking_count"));
                assertEquals(1, result.getInt("passenger_count"));
            }
        }
    }
}
