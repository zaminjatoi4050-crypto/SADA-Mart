# Execution plan

1. Foundation: JDK 21 / JavaFX 21 / Maven, diagnostic, dependency review, test harness. IN PROGRESS.
2. Dedicated Oracle schema, Liquibase migrations, application context and secure bootstrap wizard. NOT STARTED. Require disposable Oracle environment; migration design must be tested without affecting existing objects.
3. Authentication, granular service authorization, account administration and audit. NOT STARTED.
4. Design system, accessible shell, live theme switching and purpose-built screens. NOT STARTED.
5. Product, supplier and purchase/receiving vertical slice; Oracle transaction and concurrent stock tests. NOT STARTED.
6. POS: server-side price, tax and stock validation; idempotent atomic sale, ledger, payments, receipt; returns and original cost accounting. NOT STARTED.
7. Customers, shifts, expenses, financial reporting, exports, settings and backup/recovery. NOT STARTED.
8. UI accessibility/visual testing, penetration testing, concurrency, operational recovery, physical printer testing. NOT STARTED.
9. Windows packaging on Windows and actual installation acceptance; source archive only after completion. BLOCKED on platform and prior phases.

For each slice: acceptance tests and threat model, migration, service, UI, compile, unit tests, real Oracle integration tests, actual visual inspection, then update status. No verified status without evidence. Avoid connecting to or modifying any user database until explicitly configured and safe to do so.
