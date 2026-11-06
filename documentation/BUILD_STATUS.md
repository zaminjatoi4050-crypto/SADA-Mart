# Build status — 2026-11-05

Definitions: IMPLEMENTED means code exists; TESTED means relevant checks executed; VERIFIED means the relevant acceptance criteria have been demonstrated. No business module is verified.

| Component | Status | Evidence / limitation |
|---|---|---|
| Environment and build | TESTED | Java 21/JavaFX 21 migration verified on Windows with `mvnw.cmd -B clean verify`; 10 tests passed with zero failures/errors/skips. |
| Cart arithmetic | TESTED | Four JUnit tests passed for tax-exclusive arithmetic and validation; no sale, payment or database persistence. |
| JavaFX workspace shell | IN PROGRESS | Complete GUI redesign pass applied across shell, navigation, dashboard surfaces, tables, cards, profile/status controls and light/dark styling. Fresh JavaFX launch confirmed on Windows; full visual inspection across target resolutions remains outstanding. |
| Overview screen launch | TESTED | Opened under Linux Xvfb at 1600x900; screenshot `test-reports/gui-overview.png`. Only the initial light overview was visually inspected. Other pages, themes and resolutions have not been visually verified. |
| Light/dark theme | IMPLEMENTED | Theme toggle and Java Preferences storage exist, but interaction and dark appearance have not been tested. |
| Connection diagnostic | IMPLEMENTED | Background JDBC attempt, rejects literal SYS/SYSTEM; not tested against Oracle. Does not configure a store or perform migrations. |
| Non-web Spring context | IMPLEMENTED | `SadaMartApplication` provides an embedded context entry point with database and Liquibase auto-configuration disabled until explicit Oracle setup exists; not compiled on the current JDK. |
| Payment allocation domain | TESTED | BigDecimal split-payment, underpayment and cash-change calculations with focused JUnit tests; included in the Java 21 clean verification (8 tests passed). |
| Retail actions, forms, search and filters | NOT STARTED | Action buttons explicitly report unavailable; fields do not query or filter data. UI panels are not complete operational workflows. |
| Oracle connection / schema / migrations | BLOCKED | Local Oracle Free 26ai service and listener are running; `tnsping localhost:1521/FREEPDB1` succeeds. No dedicated application credentials or schema have been supplied/configured, so JDBC authentication, migrations and schema tests remain unexecuted. |
| Spring context / services / security | NOT STARTED | Dependencies alone do not implement services or authorization. |
| Setup wizard, login, roles, audit | NOT STARTED | No administrator exists. |
| Products, suppliers, purchasing, inventory, POS, payments, receipts and returns | NOT STARTED | UI previews only; no schema, stock ledger, financial services or workflows. |
| Customers, expenses, shifts, reports, exports, backup | NOT STARTED | UI previews only. |
| Oracle integration, concurrency, rollback, security, automated UI and acceptance tests | BLOCKED | No Oracle instance; tests not written or executed. |
| Windows installer and clean installation | BLOCKED | Linux host, product incomplete. |
| Source ZIP | IMPLEMENTED | Work-in-progress source archive; not a complete product. |

Warnings: Maven/Guice uses a terminally deprecated `sun.misc.Unsafe` method; JavaFX native-access behavior and Oracle connectivity remain unverified on this Windows host. Assess before release. Do not use in a live store.
