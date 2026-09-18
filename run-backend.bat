@echo off
title EXAMCHAIN Backend (Port 8080)
set "JAVA_HOME=C:\Program Files\Java\jdk-25.0.4"
cd /d "%~dp0backend"
"%JAVA_HOME%\bin\java.exe" -jar "target\examchain-backend-0.1.0-SNAPSHOT.jar" --spring.profiles.active=dev
pause
