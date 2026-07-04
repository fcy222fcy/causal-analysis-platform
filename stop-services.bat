@echo off
chcp 65001 >nul

REM ============================================
REM 智慧养殖环境异常溯源与因果分析系统
REM 停止所有服务
REM ============================================

echo ============================================
echo 停止所有服务
echo ============================================
echo.

REM 停止前端服务
echo [INFO] 停止前端服务...
taskkill /FI "WINDOWTITLE eq Frontend Service*" /F >nul 2>nul
taskkill /IM node.exe /F >nul 2>nul
echo [SUCCESS] 前端服务已停止

REM 停止因果分析服务
echo [INFO] 停止因果分析服务...
taskkill /FI "WINDOWTITLE eq Causal Service*" /F >nul 2>nul
taskkill /IM python.exe /F >nul 2>nul
echo [SUCCESS] 因果分析服务已停止

REM 停止后端服务
echo [INFO] 停止后端服务...
taskkill /FI "WINDOWTITLE eq Backend Service*" /F >nul 2>nul
echo [SUCCESS] 后端服务已停止

echo.
echo ============================================
echo 所有服务已停止
echo ============================================
pause
