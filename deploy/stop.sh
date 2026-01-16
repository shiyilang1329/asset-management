#!/bin/bash

# 资产管理系统停止脚本

APP_NAME="asset-management"
PID_FILE="./app.pid"

# 检查PID文件是否存在
if [ ! -f ${PID_FILE} ]; then
    echo "${APP_NAME} is not running"
    exit 1
fi

# 读取PID
PID=$(cat ${PID_FILE})

# 检查进程是否存在
if ! ps -p ${PID} > /dev/null 2>&1; then
    echo "${APP_NAME} is not running (stale PID file)"
    rm -f ${PID_FILE}
    exit 1
fi

# 停止应用
echo "Stopping ${APP_NAME} (PID: ${PID})..."
kill ${PID}

# 等待进程结束
for i in {1..30}; do
    if ! ps -p ${PID} > /dev/null 2>&1; then
        echo "${APP_NAME} stopped successfully"
        rm -f ${PID_FILE}
        exit 0
    fi
    sleep 1
done

# 如果还没停止，强制kill
echo "Force stopping ${APP_NAME}..."
kill -9 ${PID}
rm -f ${PID_FILE}
echo "${APP_NAME} force stopped"
