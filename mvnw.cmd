@echo off
setlocal

set "LOCAL_MVN=C:\Users\danie\.m2\wrapper\dists\apache-maven-3.8.5-bin\5i5jha092a3i37g0paqnfr15e0\apache-maven-3.8.5\bin\mvn.cmd"

if exist "%LOCAL_MVN%" (
    call "%LOCAL_MVN%" %*
    exit /b %ERRORLEVEL%
)

where mvn >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    mvn %*
    exit /b %ERRORLEVEL%
)

echo [ERROR] No se encontro Maven. Configure MAVEN_HOME o agregue mvn al PATH.
exit /b 1
