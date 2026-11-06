# Preliminary threat model — not a security audit

No penetration test or complete audit has been performed. Do not deploy.

## Critical / release blockers
- All authentication and backend authorization are missing. No privileged retail operations are implemented; future services must deny by default.
- Financial/inventory atomicity, durable idempotency, audit and reconciliation are missing. Do not use for transactions.

## High risk / unverified
- The diagnostic connects via plain Oracle thin TCP configuration; network transport encryption and trust validation are not configured. Use only in an isolated trusted test environment until TCPS is designed and tested.
- Credentials are present in JavaFX control and immutable Java strings during a test. They are not persisted or intentionally logged, but reliable memory erasure is not guaranteed. Review secret handling before production.
- Dependency scanning, role bypass, credential attacks, file upload, backup, database grants and logging review have not been performed.

## Other limitations
Diagnostic rejects literal SYS and SYSTEM but does not establish least-privilege authorization, prevent equivalent administrator accounts, or validate privileges. It does not migrate or write data. Do not consider connectivity evidence of safe setup. Database access should use a dedicated least-privilege account. Java exceptions are not displayed to avoid leaking credentials, but actionable sanitized operational logging remains to be designed.
