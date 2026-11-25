@echo off
rem Build, run all tests and enforce the coverage gate (used by the Wall health check).
setlocal
cd /d "%~dp0"
set JAVA_TOOL_OPTIONS=-Djavax.net.ssl.trustStoreType=Windows-ROOT
call "%~dp0mvnw.cmd" -B -q verify
exit /b %errorlevel%
