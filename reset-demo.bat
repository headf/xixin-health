@echo off
chcp 65001 >nul

set MYSQL="C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe"
set DB_USER=root
set DB_PASS=123456
set DB_NAME=xixin_health
set SQL_FILE=%~dp0reset-demo-data.sql

echo ========================================
echo  XixinHealth - Reset Demo Data
echo ========================================
echo.

if not exist %MYSQL% (
    echo [ERROR] MySQL not found at %MYSQL%
    echo Please check your MySQL installation path.
    pause
    exit /b 1
)

echo Connecting to MySQL...
%MYSQL% -u %DB_USER% -p%DB_PASS% --default-character-set=utf8mb4 %DB_NAME% < "%SQL_FILE%"

if %errorlevel% equ 0 (
    echo.
    echo [SUCCESS] Demo data has been reset!
    echo.
    echo All accounts password: Passw0rd!
    echo.
    echo Admin:   13900000000
    echo Nurse:   13800000100
    echo Doctor:  13800000200
    echo Review:  13800000300
    echo ZhangSan:13800000001
    echo LiSi:    13800000002
    echo WangWu:  13800000003
    echo ZhaoLiu: 13800000004
    echo SunQi:   13800000005
) else (
    echo.
    echo [ERROR] Failed to reset demo data.
    echo Make sure MySQL is running on port 3306.
)

echo.
pause
