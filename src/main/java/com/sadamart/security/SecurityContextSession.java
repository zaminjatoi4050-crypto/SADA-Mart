package com.sadamart.security;

import com.sadamart.domain.UserRole;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

public final class SecurityContextSession {
    private static final Duration SESSION_TIMEOUT = Duration.ofMinutes(30);

    private String username;
    private String fullName;
    private UserRole role;
    private Instant lastActivityAt;

    public synchronized void establishSession(String username, String fullName, UserRole role) {
        this.username = Objects.requireNonNull(username, "username");
        this.fullName = Objects.requireNonNull(fullName, "fullName");
        this.role = Objects.requireNonNull(role, "role");
        this.lastActivityAt = Instant.now();
    }

    public synchronized void clearSession() {
        this.username = null;
        this.fullName = null;
        this.role = null;
        this.lastActivityAt = null;
    }

    public synchronized boolean isAuthenticated() {
        if (username == null || role == null || lastActivityAt == null) {
            return false;
        }
        if (Duration.between(lastActivityAt, Instant.now()).compareTo(SESSION_TIMEOUT) > 0) {
            clearSession();
            return false;
        }
        this.lastActivityAt = Instant.now();
        return true;
    }

    public synchronized void requireAuthenticated() {
        if (!isAuthenticated()) {
            throw new AccessDeniedException("Authentication required: No active store session");
        }
    }

    public synchronized void requireRole(UserRole... allowedRoles) {
        requireAuthenticated();
        for (UserRole allowed : allowedRoles) {
            if (this.role == allowed) {
                return;
            }
        }
        throw new AccessDeniedException("Role " + this.role + " is not authorized; requires one of " + Arrays.toString(allowedRoles));
    }

    public synchronized String currentUsername() {
        requireAuthenticated();
        return username;
    }

    public synchronized String currentFullName() {
        requireAuthenticated();
        return fullName;
    }

    public synchronized UserRole currentRole() {
        requireAuthenticated();
        return role;
    }
}
