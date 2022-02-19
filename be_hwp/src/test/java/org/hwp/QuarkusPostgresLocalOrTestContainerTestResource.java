package org.hwp;

import java.net.Socket;
import java.util.Collections;
import java.util.Map;

import org.testcontainers.containers.PostgreSQLContainer;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class QuarkusPostgresLocalOrTestContainerTestResource implements QuarkusTestResourceLifecycleManager {

    static PostgreSQLContainer<?> db = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("postgres")
            .withUsername("postgres")
            .withPassword("postgres");

    @Override
    public Map<String, String> start() {

        if (isSthAvailableAtPort(5432)) {
            // postgresql is probably already running locally
            return Collections.emptyMap();
        }

        db.start();
        return Map.of("quarkus.datasource.jdbc.url", db.getJdbcUrl());
    }

    private static boolean isSthAvailableAtPort(int port) {
        try {
            try (Socket s = new Socket("localhost", port)) {
                return true;// connection succeeded
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    @Override
    public void stop() {
        db.stop(); // no problem if not running
    }
}
