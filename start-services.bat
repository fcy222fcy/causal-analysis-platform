@echo off
chcp 65001 >nul

REM ============================================
REM 智慧养殖环境异常溯源与因果分析系统
REM Smart Farming Environment Anomaly
REM Traceability and Causal Analysis System
REM ============================================

REM 设置窗口标题
title 智慧养殖环境异常溯源与因果分析系统

echo ============================================
echo 智慧养殖环境异常溯源与因果分析系统
echo Smart Farming Environment Anomaly
echo Traceability and Causal Analysis System
echo ============================================
echo.

REM 设置项目根目录
set PROJECT_ROOT=%~dp0

REM 服务端口
set FRONTEND_PORT=5173
set CAUSAL_SERVICE_PORT=5000
set BACKEND_PORT=8080

REM 检查依赖
echo [INFO] 检查依赖...

REM 检查 Node.js
where node >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Node.js 未安装
    pause
    exit /b 1
)
for /f "tokens=*" %%i in ('node -v') do echo [SUCCESS] Node.js: %%i

REM 检查 npm
where npm >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] npm 未安装
    pause
    exit /b 1
)
for /f "tokens=*" %%i in ('npm -v') do echo [SUCCESS] npm: %%i

REM 检查 Python
where python >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Python 未安装
    pause
    exit /b 1
)
for /f "tokens=*" %%i in ('python --version') do echo [SUCCESS] Python: %%i

REM 检查 Java
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Java 未安装
    pause
    exit /b 1
)
for /f "tokens=*" %%i in ('java -version 2^>^&1 ^| findstr /i "version"') do echo [SUCCESS] Java: %%i

REM 检查 Maven
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo [WARNING] Maven 未安装，将尝试使用 Maven Wrapper
) else (
    for /f "tokens=*" %%i in ('mvn -v 2^>^&1 ^| findstr /i "Maven"') do echo [SUCCESS] Maven: %%i
)

echo.

REM 启动因果分析服务
echo ============================================
echo [INFO] 启动因果分析服务
echo ============================================

cd /d "%PROJECT_ROOT%causal-service"

REM 检查 Python 虚拟环境
if not exist "venv" (
    echo [INFO] 创建 Python 虚拟环境...
    python -m venv venv
)

REM 激活虚拟环境
call venv\Scripts\activate.bat

REM 检查依赖是否安装
python -c "import fastapi" 2>nul
if %errorlevel% neq 0 (
    echo [INFO] 安装因果分析服务依赖...
    pip install -r requirements.txt
)

REM 启动 FastAPI 服务
echo [INFO] 启动 FastAPI 服务...
start "Causal Service" python app.py
timeout /t 3 /nobreak >nul
echo [SUCCESS] 因果分析服务已启动: http://localhost:%CAUSAL_SERVICE_PORT%
echo.

REM 启动后端服务
echo ============================================
echo [INFO] 启动后端服务
echo ============================================

cd /d "%PROJECT_ROOT%smart-farming-backend"

REM 启动 Spring Boot 服务
echo [INFO] 启动 Spring Boot 服务...
if exist "mvnw.cmd" (
    start "Backend Service" mvnw.cmd spring-boot:run
) else (
    start "Backend Service" mvn spring-boot:run
)
timeout /t 5 /nobreak >nul
echo [SUCCESS] 后端服务已启动: http://localhost:%BACKEND_PORT%
echo.

REM 启动前端服务
echo ============================================
echo [INFO] 启动前端服务
echo ============================================

cd /d "%PROJECT_ROOT%frontend"

REM 检查 node_modules 是否存在
if not exist "node_modules" (
    echo [INFO] 安装前端依赖...
    call npm install
)

REM 启动 Vue.js 开发服务器
echo [INFO] 启动 Vue.js 开发服务器...
start "Frontend Service" npm run dev
timeout /t 3 /nobreak >nul
echo [SUCCESS] 前端服务已启动: http://localhost:%FRONTEND_PORT%
echo.

REM 显示服务状态
echo ============================================
echo 服务状态
echo ============================================
echo.
echo 前端服务:      http://localhost:%FRONTEND_PORT%
echo 因果分析服务:  http://localhost:%CAUSAL_SERVICE_PORT%
echo 后端服务:      http://localhost:%BACKEND_PORT%
echo API 文档:      http://localhost:%BACKEND_PORT%/doc.html
echo H2 控制台:     http://localhost:%BACKEND_PORT%/h2-console
echo.
echo ============================================
echo 按任意键停止所有服务...
echo ============================================
pause >nul

REM 停止所有服务
echo.
echo [INFO] 停止所有服务...

REM 停止前端服务
taskkill /FI "WINDOWTITLE eq Frontend Service*" /F >nul 2>nul

REM 停止因果分析服务
taskkill /FI "WINDOWTITLE eq Causal Service*" /F >nul 2>nul

REM 停止后端服务
taskkill /FI "WINDOWTITLE eq Backend Service*" /F >nul 2>nul

echo [SUCCESS] 所有服务已停止
pause
