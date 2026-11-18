@echo off
setlocal
cd /d "%~dp0"
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0Start-Demo.ps1" %*
if errorlevel 1 (
  echo.
  echo Demo startup failed. Review the message above.
  pause
)
endlocal
