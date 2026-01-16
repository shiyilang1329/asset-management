#!/bin/bash

# 资产管理系统状态查看脚本

APP_NAME="asset-management"
PID_FILE="./app.pid"

# 检查PID文件
if [ ! -f ${PID_FILE} ]; then
    echo "${APP_NAME} is not running"
    exit 1
fi

# 读取PID
PID=$(cat ${PID_FILE})

# 检查进程
if ps -p ${PID} > /dev/null 2>&1; then
    echo "${APP_NAME} is running (PID: ${PID})"
    
    # 显示进程信息
    echo ""
    echo "Process info:"
    ps -p ${PID} -o pid,ppid,cmd,%mem,%cpu,etime
    
    # 显示端口监听
    echo ""
    echo "Listening ports:"
    netstat -tlnp 2>/dev/null | grep ${PID} || ss -tlnp 2>/dev/null | grep ${PID}
    
    exit 0
else
    echo "${APP_NAME} is not running (stale PID file)"
    rm -f ${PID_FILE}
    exit 1
fi
