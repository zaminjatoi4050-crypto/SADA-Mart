package com.sadamart.infrastructure;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OracleDatabasePropertiesTest {
    @Test
    void buildsThinAndTcpsUrlsWithoutChangingCredentials() {
        var plain = new OracleDatabaseProperties(true, "db.example", 1521, "FREEPDB1", "app_user", "secret", false);
        var tcps = new OracleDatabaseProperties(true, "db.example", 2484, "APP", "app_user", "secret", true);

        assertEquals("jdbc:oracle:thin:@//db.example:1521/FREEPDB1", plain.jdbcUrl());
        assertEquals("jdbc:oracle:thin:@tcps://db.example:2484/APP", tcps.jdbcUrl());
    }

    @Test
    void rejectsPrivilegedAccountsAndMissingSecrets() {
        var sys = new OracleDatabaseProperties(true, "db.example", 1521, "FREEPDB1", "SYS", "secret", false);
        var missingPassword = new OracleDatabaseProperties(true, "db.example", 1521, "FREEPDB1", "app_user", "", false);

        assertThrows(IllegalArgumentException.class, sys::validate);
        assertThrows(IllegalArgumentException.class, missingPassword::validate);
    }
}
