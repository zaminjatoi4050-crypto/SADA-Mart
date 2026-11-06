# Development installation

This is not a Windows installer. Install JDK 21 and use `mvnw.cmd verify` and `mvnw.cmd javafx:run` from a graphical Windows desktop; the wrapper downloads Maven and dependencies. The Java 21 verification passes on this host. A separate Oracle server and a dedicated account are needed for the diagnostic. The local Oracle Free 26ai listener responds at `localhost:1521/FREEPDB1`, but no application credentials have been configured. The application does not configure, migrate or modify Oracle yet. Consult Oracle's official installation and least-privilege guidance for a disposable development instance. No destructive database setup or restore script is supplied.

Production installation, Oracle schema provisioning, TCPS, database backup/restore, Data Pump, printer setup and recovery instructions must be written and tested before release.
