@echo off
chcp 65001 >nul

REM ============================================
REM Stop All Services
REM ============================================

echo ============================================
echo Stopping All Services
echo ============================================
echo.

echo [INFO] Stopping Frontend Service...
taskkill /FI "WINDOWTITLE eq Frontend Service*" /F >nul 2>nul
taskkill /IM node.exe /F >nul 2>nul
echo [SUCCESS] Frontend Service stopped

echo [INFO] Stopping Causal Analysis Service...
taskkill /FI "WINDOWTITLE eq Causal Service*" /F >nul 2>nul
taskkill /IM python.exe /F >nul 2>nul
echo [SUCCESS] Causal Analysis Service stopped

echo [INFO] Stopping Backend Service...
taskkill /FI "WINDOWTITLE eq Backend Service*" /F >nul 2>nul
echo [SUCCESS] Backend Service stopped

echo.
echo ============================================
echo All Services Stopped
echo ============================================
pause
