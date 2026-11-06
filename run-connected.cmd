@echo off
setlocal

if not exist "%JAVA_HOME%\bin\java.exe" set "JAVA_HOME=C:\Program Files\Microsoft\jdk-21.0.13.21-hotspot"
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo JDK 21 was not found. Set JAVA_HOME to a valid JDK 21 folder.
    exit /b 1
)
set "SADAMART_ORACLE_ENABLED=true"
set "SADAMART_ORACLE_HOST=localhost"
set "SADAMART_ORACLE_PORT=1521"
set "SADAMART_ORACLE_SERVICE=FREEPDB1"
set "SADAMART_ORACLE_USERNAME=sadamart_app"
set "SADAMART_ORACLE_TCPS=false"

if not defined SADAMART_ORACLE_PASSWORD (
    echo Enter the Oracle password for sadamart_app.
    set /p "SADAMART_ORACLE_PASSWORD=Password: "
)

if not defined SADAMART_ORACLE_PASSWORD (
    echo Oracle password is required.
    exit /b 1
)

call "%~dp0mvnw.cmd" javafx:run
set "EXIT_CODE=%ERRORLEVEL%"
set "SADAMART_ORACLE_PASSWORD="
endlocal & exit /b %EXIT_CODE%
