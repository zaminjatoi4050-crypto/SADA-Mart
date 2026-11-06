# Test strategy and evidence

Current executed: `mvnw.cmd -B clean verify` under Microsoft OpenJDK 21.0.13 on Windows; 8 JUnit tests, zero failures/errors/skips. JaCoCo is configured and generates a report under `target/site/jacoco` locally. No coverage target is claimed based on this small foundation.

JavaFX launch attempt failed because no DISPLAY: `test-reports/javafx-run.log`. No UI, visual, Oracle integration, concurrency, access control, recovery, printer or Windows installer tests were executed. They are BLOCKED, not passed. H2 must not substitute for Oracle compatibility evidence. The final suite must cover all scenarios in the acceptance brief, include hostile authorization calls and real Oracle rollback/idempotency/stock races, and report tested scopes and coverage honestly.
