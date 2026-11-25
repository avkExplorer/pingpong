@echo off
rem Builds the jar (tests run), then starts the app and a throwaway database: http://127.0.0.1:4820/
setlocal
cd /d "%~dp0"
rem Lets Java trust the Windows certificate store (Norton re-signs HTTPS on this PC).
set JAVA_TOOL_OPTIONS=-Djavax.net.ssl.trustStoreType=Windows-ROOT
call "%~dp0mvnw.cmd" -B -q verify || exit /b 1
docker compose -f compose.local.yaml up -d --build
