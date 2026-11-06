# SADA Mart — development foundation

**Not a finished retail application. Do not use for live sales or store data.**

This repository currently contains a JavaFX/FXML workspace with thirteen early module previews, an Oracle connection diagnostic and a separately tested, tax-exclusive cart calculation library. It does not contain setup, authentication, migrations, POS, financial persistence, reporting, backups or an installer. Spring dependencies are present but no Spring application context or security policy is implemented. See `documentation/BUILD_STATUS.md` before proceeding.

## Development prerequisites
JDK 21, network access for Maven dependencies, a supported graphical desktop, and optionally an Oracle instance reachable through TCP or TCPS with a dedicated application user. Oracle must be separately installed; do not use SYS or SYSTEM. No password is included or saved. No live database has been tested.

```sh
./mvnw verify
./mvnw javafx:run
```
On Windows use `mvnw.cmd` in place of `./mvnw`. Maven Wrapper downloads Maven. `javafx:run` opens the development workspace, not the finished app; the sidebar opens the connection diagnostic. JavaFX needs an accessible graphical display. The build does not produce a self-contained Windows installer.

Oracle configuration is opt-in and environment-driven. Set `SADAMART_ORACLE_ENABLED=true`, `SADAMART_ORACLE_HOST`, `SADAMART_ORACLE_PORT`, `SADAMART_ORACLE_SERVICE`, `SADAMART_ORACLE_USERNAME`, `SADAMART_ORACLE_PASSWORD`, and optionally `SADAMART_ORACLE_TCPS=true`. Use a dedicated least-privilege account; migrations and store setup are not implemented yet.

## Money policy in the current calculation library
Amounts use two decimal places and HALF_UP rounding; quantities support three decimal places; each extended line is rounded to two decimals before discounts, then tax is rounded once per order. Prices are tax-exclusive, and the caller supplies a rate; no tax rate is assumed. Currency-specific decimal precision and legal/tax requirements require further design. This library does not process payments or commit transactions.
