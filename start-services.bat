@echo off
chcp 65001 >nul

REM ============================================
REM Smart Farming Environment Anomaly
REM Traceability and Causal Analysis System
REM ============================================

title Smart Farming System

echo ============================================
echo Smart Farming Environment Anomaly
echo Traceability and Causal Analysis System
echo ============================================
echo.

set PROJECT_ROOT=%~dp0

set FRONTEND_PORT=3000
set CAUSAL_SERVICE_PORT=5000
set BACKEND_PORT=8080

echo [INFO] Checking dependencies...

where node >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Node.js not installed
    pause
    exit /b 1
)
for /f "tokens=*" %%i in ('node -v') do echo [SUCCESS] Node.js: %%i

where npm >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] npm not installed
    pause
    exit /b 1
)
for /f "tokens=*" %%i in ('npm -v') do echo [SUCCESS] npm: %%i

where python >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Python not installed
    pause
    exit /b 1
)
for /f "tokens=*" %%i in ('python --version') do echo [SUCCESS] Python: %%i

where java >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Java not installed
    pause
    exit /b 1
)
for /f "tokens=*" %%i in ('java -version 2^>^&1 ^| findstr /i "version"') do echo [SUCCESS] Java: %%i

where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo [WARNING] Maven not installed, will use Maven Wrapper
) else (
    for /f "tokens=*" %%i in ('mvn -v 2^>^&1 ^| findstr /i "Maven"') do echo [SUCCESS] Maven: %%i
)

echo.

echo ============================================
echo [INFO] Starting Causal Analysis Service
echo ============================================

cd /d "%PROJECT_ROOT%causal-service"

if not exist "venv" (
    echo [INFO] Creating Python virtual environment...
    python -m venv venv
)

call venv\Scripts\activate.bat

python -c "import fastapi" 2>nul
if %errorlevel% neq 0 (
    echo [INFO] Installing causal service dependencies...
    pip install -r requirements.txt
)

echo [INFO] Starting FastAPI service...
start "Causal Service" python app.py
timeout /t 3 /nobreak >nul
echo [SUCCESS] Causal service started: http://localhost:%CAUSAL_SERVICE_PORT%
echo.

echo ============================================
echo [INFO] Starting Backend Service
echo ============================================

cd /d "%PROJECT_ROOT%smart-farming-backend"

echo [INFO] Starting Spring Boot service...
if exist "mvnw.cmd" (
    start "Backend Service" mvnw.cmd spring-boot:run
) else (
    start "Backend Service" mvn spring-boot:run
)
timeout /t 5 /nobreak >nul
echo [SUCCESS] Backend service started: http://localhost:%BACKEND_PORT%
echo.

echo ============================================
echo [INFO] Starting Frontend Service
echo ============================================

cd /d "%PROJECT_ROOT%frontend"

if not exist "node_modules" (
    echo [INFO] Installing frontend dependencies...
    call npm install
)

echo [INFO] Starting Vue.js dev server...
start "Frontend Service" npm run dev
timeout /t 3 /nobreak >nul
echo [SUCCESS] Frontend service started: http://localhost:%FRONTEND_PORT%
echo.

echo ============================================
echo Service Status
echo ============================================
echo.
echo Frontend:          http://localhost:%FRONTEND_PORT%
echo Causal Service:    http://localhost:%CAUSAL_SERVICE_PORT%
echo Backend Service:   http://localhost:%BACKEND_PORT%
echo API Documentation: http://localhost:%BACKEND_PORT%/doc.html
echo H2 Console:        http://localhost:%BACKEND_PORT%/h2-console
echo.
echo ============================================
echo Press any key to stop all services...
echo ============================================
pause >nul

echo.
echo [INFO] Stopping all services...

taskkill /FI "WINDOWTITLE eq Frontend Service*" /F >nul 2>nul
taskkill /FI "WINDOWTITLE eq Causal Service*" /F >nul 2>nul
taskkill /FI "WINDOWTITLE eq Backend Service*" /F >nul 2>nul

echo [SUCCESS] All services stopped
pause
