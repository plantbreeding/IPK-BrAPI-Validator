@echo off
rem Runs copy-to-server.ps1 regardless of the PowerShell execution policy.
rem Example: deploy\copy-to-server.cmd user@server -Deploy
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0copy-to-server.ps1" %*
exit /b %ERRORLEVEL%
