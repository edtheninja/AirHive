package com.airhive.backend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.airhive.backend.entity.GovernanceLock;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Timeout;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class GovernanceLockRepositoryIntegrationTest {

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18.6");

    static {
        POSTGRES.start();

        Flyway.configure()
                .dataSource(
                        POSTGRES.getJdbcUrl(),
                        POSTGRES.getUsername(),
                        POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add(
                "spring.datasource.driver-class-name",
                () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    private GovernanceLockRepository governanceLockRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void migrationCreatesSingletonGovernanceLock() {
        GovernanceLock lock = governanceLockRepository.findById(1L)
                .orElseThrow();

        assertEquals(1L, lock.getId());
    }

    @Test
    @Timeout(15)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void concurrentGovernanceTransactionsShouldBeSerialized() throws Exception {

        CountDownLatch firstLockAcquired = new CountDownLatch(1);
        CountDownLatch releaseFirstTransaction = new CountDownLatch(1);

        CountDownLatch secondTransactionStarted = new CountDownLatch(1);
        CountDownLatch secondLockAcquired = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        Future<?> firstTransaction = null;
        Future<?> secondTransaction = null;

        try {
            // Transaction A acquires the lock and holds it.
            firstTransaction = executor.submit(() -> transactionTemplate.executeWithoutResult(status -> {

                governanceLockRepository.lockById(1L)
                        .orElseThrow(() -> new AssertionError(
                                "Governance lock row is missing"));

                firstLockAcquired.countDown();

                try {
                    if (!releaseFirstTransaction.await(
                            5, TimeUnit.SECONDS)) {
                        throw new IllegalStateException(
                                "Timed out waiting to release first transaction");
                    }
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(
                            "First transaction was interrupted", exception);
                }
            }));

            // Ensure Transaction A has acquired the lock.
            assertTrue(
                    firstLockAcquired.await(5, TimeUnit.SECONDS),
                    "First transaction should acquire the governance lock");

            // Transaction B attempts to acquire the same lock.
            secondTransaction = executor.submit(() -> {
                secondTransactionStarted.countDown();

                transactionTemplate.executeWithoutResult(status -> {

                    governanceLockRepository.lockById(1L)
                            .orElseThrow(() -> new AssertionError(
                                    "Governance lock row is missing"));

                    secondLockAcquired.countDown();
                });
            });

            assertTrue(
                    secondTransactionStarted.await(5, TimeUnit.SECONDS),
                    "Second transaction should start");

            // Transaction B must remain blocked while A holds the lock.
            assertFalse(
                    secondLockAcquired.await(300, TimeUnit.MILLISECONDS),
                    "Second transaction must wait while the first holds the lock");

            // Release A. B should now be able to acquire the lock.
            releaseFirstTransaction.countDown();

            firstTransaction.get(5, TimeUnit.SECONDS);
            secondTransaction.get(5, TimeUnit.SECONDS);

            assertTrue(
                    secondLockAcquired.getCount() == 0,
                    "Second transaction should acquire the lock after the first commits");

        } finally {
            // Prevent a failed assertion from leaving Transaction A blocked.
            releaseFirstTransaction.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void lockByIdAcquiresPessimisticWriteLock() {
        GovernanceLock lock = governanceLockRepository.lockById(1L)
                .orElseThrow();

        assertEquals(1L, lock.getId());
        assertTrue(governanceLockRepository.existsById(1L));
    }
}