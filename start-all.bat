@echo off
chcp 65001 >nul
title 熙心健康管理系统

echo ========================================
echo  熙心健康体检管理系统 - 一键启动
echo ========================================

:: ===== 1. 启动 MySQL =====
echo [1/4] 启动 MySQL 8.4 ...
tasklist /FI "IMAGENAME eq mysqld.exe" 2>nul | find /i "mysqld" >nul
if %errorlevel% equ 0 (
    echo   -> MySQL 已在运行中
) else (
    start "MySQL" /MIN "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysqld" ^
        --datadir="C:\Users\30669\AppData\Local\xixin-health-mysql-data" ^
        --port=3306 ^
        --log-error="C:\Users\30669\AppData\Local\Temp\mysql-start.log"
    timeout /t 8 /nobreak >nul
    echo   -> MySQL 已启动
)

:: ===== 2. 启动后端 =====
echo [2/4] 启动后端 (Spring Boot :8080) ...
start "Backend" /MIN "cmd /c" cd /d "%~dp0backend" ^&^& .\mvnw spring-boot:run -DskipTests
timeout /t 3 /nobreak >nul
echo   -> 后端正在启动...

:: ===== 3. 启动前端 =====
echo [3/4] 启动管理后台 (Admin :5173) ...
start "Admin" /MIN cmd /c "cd /d "%~dp0admin" && npx vite --host 0.0.0.0 --port 5173"

echo [4/4] 启动员工端 (Staff :3000) ...
start "Staff" /MIN cmd /c "cd /d "%~dp0staff" && npx vite --host 0.0.0.0 --port 3000"

echo.
echo ========================================
echo  所有服务正在启动...
echo  管理后台: http://localhost:5173/admin/
echo  员工端:   http://localhost:3000/
echo  用户端:   http://localhost:8081/
echo  后端 API: http://localhost:8080
echo ========================================
echo.
echo  按任意键关闭此窗口(后台服务继续运行)
pause >nul
