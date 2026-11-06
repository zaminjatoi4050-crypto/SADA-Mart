# Architecture and dependency decisions

Intended architecture: JavaFX/FXML front end calls application services in-process; a non-web Spring context enforces permissions and transaction boundaries. Oracle via Hikari/JPA/JDBC is authoritative, with Liquibase migrations and a dedicated least-privilege schema. No application context, database schema, migrations or business services have been implemented yet. Current UI is an isolated JDBC diagnostic, deliberately not a setup workflow. The pure cart calculator has no persistence or access control and is not a POS implementation.

## Dependency catalog (2026-11-05)
- JDK 21 LTS, JavaFX 21.0.6, selected as the aligned Java 21 baseline.
- Spring Boot 4.1.2 parent and managed Spring Security, Data JPA, Hibernate, validation and Liquibase dependencies. Boot 4.1.2 documentation says Java 17–27; integration is not validated.
- Oracle `ojdbc17` 23.26.2.0.0: retained for Java 17+ Oracle connectivity. No Oracle connection has been demonstrated.
- JUnit via Spring Boot dependency management; JaCoCo 0.8.14; Maven 3.9.11; JavaFX Maven plugin 0.0.8.
- AtlantaFX 3.0.0 explicitly requires JavaFX 27+ and is therefore omitted from the JavaFX 21 baseline; custom CSS is used. A maintained icon library and TestFX remain to be selected after compatibility testing.

Sources: https://docs.spring.io/spring-boot/system-requirements.html ; https://mail.openjdk.org/pipermail/openjfx-dev/2025-September/056290.html ; https://www.oracle.com/database/technologies/faq-jdbc.html ; https://oracle.com/co/database/technologies/appdev/jdbc-downloads.html ; https://github.com/mkpaz/atlantafx/releases/tag/v3.0.0 .

Future invariant: an idempotency key and unique constraint must protect completion; lock inventory in stable order, reprice/recalculate inside one transaction, persist immutable sale costs, payment allocations and stock movements atomically, and acknowledge success only after commit. Returns must lock and account for prior returns. Weighted average cost at receipt is the planned policy. None of these are implemented or verified.
