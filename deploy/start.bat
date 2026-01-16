@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

:: 资产管理系统启动脚本 (Windows)

set APP_NAME=asset-management
set JAR_FILE=asset-management-1.0.0.jar
set CONFIG_DIR=.\config
set LOG_DIR=.\logs
set PID_FILE=app.pid

:: 创建日志目录
if not exist %LOG_DIR% mkdir %LOG_DIR%

:: 检查是否已经运行
if exist %PID_FILE% (
    set /p PID=<%PID_FILE%
    tasklist /FI "PID eq !PID!" 2>nul | find "java.exe" >nul
    if !errorlevel! equ 0 (
        echo %APP_NAME% is already running (PID: !PID!)
        exit /b 1
    ) else (
        del %PID_FILE%
    )
)

:: JVM参数配置
set JVM_OPTS=-Xms512m -Xmx1024m
set JVM_OPTS=%JVM_OPTS% -XX:+UseG1GC
set JVM_OPTS=%JVM_OPTS% -XX:MaxGCPauseMillis=200
set JVM_OPTS=%JVM_OPTS% -XX:+HeapDumpOnOutOfMemoryError
set JVM_OPTS=%JVM_OPTS% -XX:HeapDumpPath=%LOG_DIR%\heap_dump.hprof

:: Spring Boot配置
set SPRING_OPTS=--spring.config.location=%CONFIG_DIR%\application.yml
set SPRING_OPTS=%SPRING_OPTS% --logging.config=%CONFIG_DIR%\logback-spring.xml
set SPRING_OPTS=%SPRING_OPTS% --spring.profiles.active=prod

:: 启动应用
echo Starting %APP_NAME%...
start /b java %JVM_OPTS% -jar %JAR_FILE% %SPRING_OPTS% > %LOG_DIR%\console.log 2>&1

:: 等待进程启动
timeout /t 2 /nobreak >nul

:: 获取PID
for /f "tokens=2" %%a in ('tasklist /FI "IMAGENAME eq java.exe" /FO LIST ^| find "PID:"') do (
    set JAVA_PID=%%a
    goto :found
)

:found
echo %JAVA_PID% > %PID_FILE%
echo %APP_NAME% started successfully (PID: %JAVA_PID%)
echo Log file: %LOG_DIR%\%APP_NAME%.log
echo Console log: %LOG_DIR%\console.log

endlocal
