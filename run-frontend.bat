@echo off
title EXAMCHAIN Frontend (Port 5173)
set "PATH=C:\Users\athar\.gemini\antigravity\brain\f2a46efb-0011-4ba2-815f-5d2ac41102f8\scratch\nodejs;%PATH%"
cd /d "%~dp0frontend"
call npm run dev
pause
