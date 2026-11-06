@echo off
chcp 65001 >nul
title XixinHealth Frontend

echo ========================================
echo  XixinHealth - Start All Frontends
echo ========================================
echo.
echo  [INFO] Please start backend separately (IDEA)
echo.

set ROOT=%~dp0

:: --- Install dependencies if needed ---
if not exist "%ROOT%admin\node_modules" (
    echo [INFO] Installing admin dependencies...
    cd /d "%ROOT%admin"
    call npm install
)
if not exist "%ROOT%staff\node_modules" (
    echo [INFO] Installing staff dependencies...
    cd /d "%ROOT%staff"
    call npm install
)
if not exist "%ROOT%user-web\node_modules" (
    echo [INFO] Installing user-web dependencies...
    cd /d "%ROOT%user-web"
    call npm install
)

echo.
echo [INFO] Starting 3 frontend services...
echo.

:: --- Start each frontend in its own window ---
start "Admin-5173" cmd /k "cd /d %ROOT%admin && title Admin-5173 && npx vite --host 0.0.0.0 --port 5173"
start "Staff-3000" cmd /k "cd /d %ROOT%staff && title Staff-3000 && npx vite --host 0.0.0.0 --port 3000"
start "UserWeb-8081" cmd /k "cd /d %ROOT%user-web && title UserWeb-8081 && npx vite --host 0.0.0.0 --port 8081"

echo ========================================
echo  All frontends starting up!
echo.
echo  Admin:   http://localhost:5173
echo  Staff:   http://localhost:3000
echo  User:    http://localhost:8081
echo  Backend: http://localhost:8080 (start manually)
echo ========================================
echo.
echo  Close each window with X when done.
echo.
pause
