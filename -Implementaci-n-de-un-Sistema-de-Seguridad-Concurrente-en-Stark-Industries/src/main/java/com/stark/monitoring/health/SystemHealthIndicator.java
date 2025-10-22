package com.stark.monitoring.health;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class SystemHealthIndicator implements HealthIndicator {
    private final DataSource dataSource;

    public SystemHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        long start = System.nanoTime();
        try (Connection c = dataSource.getConnection()) {
            boolean valid = c.isValid(2);
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            Health.Builder b = valid ? Health.up() : Health.down();
            b.withDetail("db.latencyMs", latencyMs);
            return b.build();
        } catch (SQLException e) {
            return Health.down(e).build();
        }
    }
}


