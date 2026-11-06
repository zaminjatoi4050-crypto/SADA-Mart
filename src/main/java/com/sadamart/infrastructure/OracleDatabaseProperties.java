package com.sadamart.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sadamart.oracle")
public record OracleDatabaseProperties(
        boolean enabled,
        String host,
        int port,
        String service,
        String username,
        String password,
        boolean tcps
) {
    public String jdbcUrl() {
        String endpoint = require(host, "host") + ":" + port + "/" + require(service, "service");
        return tcps
                ? "jdbc:oracle:thin:@tcps://" + endpoint
                : "jdbc:oracle:thin:@//" + endpoint;
    }

    public void validate() {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Oracle port must be between 1 and 65535");
        }
        if (username == null || username.isBlank()
                || username.equalsIgnoreCase("sys")
                || username.equalsIgnoreCase("system")) {
            throw new IllegalArgumentException("A dedicated Oracle application account is required");
        }
        require(host, "host");
        require(service, "service");
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Oracle password is required when the database is enabled");
        }
    }

    private static String require(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Oracle " + name + " is required");
        }
        return value.trim();
    }
}
