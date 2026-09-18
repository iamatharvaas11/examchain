@echo off
echo ========================================================
echo Starting EXAMCHAIN System (Backend + Frontend)
echo ========================================================
start "EXAMCHAIN Backend" "%~dp0run-backend.bat"
ping 127.0.0.1 -n 4 >nul
start "EXAMCHAIN Frontend" "%~dp0run-frontend.bat"
echo.
echo Servers launched in dedicated console windows!
echo Frontend: http://localhost:5173
echo Backend:  http://localhost:8080/api/v1/health
echo ========================================================
