package com.sadamart.security;

import com.sadamart.domain.AuditLogEntry;
import com.sadamart.domain.UserAccount;
import com.sadamart.domain.UserRole;
import com.sadamart.repository.AuditLogRepository;
import com.sadamart.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
public class AuthenticationService {
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCKOUT_DURATION = Duration.ofMinutes(15);

    private final UserAccountRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final SecurityContextSession session;

    public AuthenticationService(UserAccountRepository userRepository,
                                 AuditLogRepository auditLogRepository,
                                 SecurityContextSession session) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.auditLogRepository = Objects.requireNonNull(auditLogRepository);
        this.session = Objects.requireNonNull(session);
    }

    @Transactional
    public UserAccount authenticate(String username, String rawPassword) {
        if (username == null || username.isBlank() || rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Username and password are required");
        }
        UserAccount user = userRepository.findByUsernameIgnoreCase(username.trim())
                .orElseThrow(() -> new AccessDeniedException("Invalid username or password"));

        if (!user.isActive()) {
            throw new AccessDeniedException("Account is disabled");
        }
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            throw new AccessDeniedException("Account is temporarily locked due to failed sign-in attempts");
        }

        if (!PasswordHasher.verify(rawPassword, user.getPasswordHash())) {
            int attempts = user.getFailedAttempts() + 1;
            user.setFailedAttempts(attempts);
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                user.setLockedUntil(Instant.now().plus(LOCKOUT_DURATION));
            }
            userRepository.save(user);
            auditLogRepository.save(new AuditLogEntry(
                    user.getUsername(), user.getRole().name(),
                    "SIGN_IN_FAILED", "UserAccount", user.getUsername(),
                    "Failed sign-in attempt #" + attempts
            ));
            throw new AccessDeniedException("Invalid username or password");
        }

        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        user.setLastSignInAt(Instant.now());
        userRepository.save(user);

        session.establishSession(user.getUsername(), user.getFullName(), user.getRole());
        auditLogRepository.save(new AuditLogEntry(
                user.getUsername(), user.getRole().name(),
                "SIGN_IN_SUCCESS", "UserAccount", user.getUsername(),
                "Authenticated session established"
        ));
        return user;
    }

    @Transactional
    public UserAccount provisionUser(String username, String fullName, String rawPassword, UserRole role) {
        session.requireRole(UserRole.ADMIN);
        if (userRepository.findByUsernameIgnoreCase(username.trim()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        String hash = PasswordHasher.hash(rawPassword);
        UserAccount created = userRepository.save(new UserAccount(username.trim(), fullName.trim(), hash, role));
        auditLogRepository.save(new AuditLogEntry(
                session.currentUsername(), session.currentRole().name(),
                "USER_PROVISIONED", "UserAccount", created.getUsername(),
                "Provisioned operator with role " + role
        ));
        return created;
    }

    @Transactional(readOnly = true)
    public List<UserAccount> listUsers() {
        session.requireAuthenticated();
        return userRepository.findAll();
    }

    public SecurityContextSession session() { return session; }
}
