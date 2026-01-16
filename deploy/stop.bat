@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

:: 资产管理系统停止脚本 (Windows)

set APP_NAME=asset-management
set PID_FILE=app.pid

:: 检查PID文件
if not exist %PID_FILE% (
    echo %APP_NAME% is not running
    exit /b 1
)

:: 读取PID
set /p PID=<%PID_FILE%

:: 检查进程是否存在
tasklist /FI "PID eq %PID%" 2>nul | find "java.exe" >nul
if !errorlevel! neq 0 (
    echo %APP_NAME% is not running (stale PID file)
    del %PID_FILE%
    exit /b 1
)

:: 停止应用
echo Stopping %APP_NAME% (PID: %PID%)...
taskkill /PID %PID% /F >nul 2>&1

:: 删除PID文件
del %PID_FILE%

echo %APP_NAME% stopped successfully

endlocal
