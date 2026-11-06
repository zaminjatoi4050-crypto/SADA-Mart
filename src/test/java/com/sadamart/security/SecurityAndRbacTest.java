package com.sadamart.security;

import com.sadamart.domain.UserRole;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SecurityAndRbacTest {
    @Test
    void hashesAndVerifiesPasswordsWithUniqueSalt() {
        String hash1 = PasswordHasher.hash("SadaMart#2026");
        String hash2 = PasswordHasher.hash("SadaMart#2026");
        assertNotEquals(hash1, hash2);
        assertTrue(PasswordHasher.verify("SadaMart#2026", hash1));
        assertFalse(PasswordHasher.verify("WrongPassword", hash1));
    }

    @Test
    void enforcesRoleBasedAccessControlAtServiceBoundary() {
        SecurityContextSession session = new SecurityContextSession();
        assertThrows(AccessDeniedException.class, session::requireAuthenticated);

        session.establishSession("zainab.pos", "Zainab Mangi", UserRole.CASHIER);
        assertDoesNotThrow(() -> session.requireRole(UserRole.CASHIER, UserRole.ADMIN));
        assertThrows(AccessDeniedException.class, () -> session.requireRole(UserRole.ADMIN));
    }
}
