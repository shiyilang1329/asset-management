#!/bin/bash

# 资产管理系统启动脚本

APP_NAME="asset-management"
JAR_FILE="asset-management-1.0.0.jar"
CONFIG_DIR="./config"
LOG_DIR="./logs"
PID_FILE="./app.pid"

# 创建必要的目录
mkdir -p ${LOG_DIR}

# 检查是否已经运行
if [ -f ${PID_FILE} ]; then
    PID=$(cat ${PID_FILE})
    if ps -p ${PID} > /dev/null 2>&1; then
        echo "${APP_NAME} is already running (PID: ${PID})"
        exit 1
    else
        rm -f ${PID_FILE}
    fi
fi

# JVM参数配置
JVM_OPTS="-Xms512m -Xmx1024m"
JVM_OPTS="${JVM_OPTS} -XX:+UseG1GC"
JVM_OPTS="${JVM_OPTS} -XX:MaxGCPauseMillis=200"
JVM_OPTS="${JVM_OPTS} -XX:+HeapDumpOnOutOfMemoryError"
JVM_OPTS="${JVM_OPTS} -XX:HeapDumpPath=${LOG_DIR}/heap_dump.hprof"

# Spring Boot配置
SPRING_OPTS="--spring.config.location=${CONFIG_DIR}/application.yml"
SPRING_OPTS="${SPRING_OPTS} --logging.config=${CONFIG_DIR}/logback-spring.xml"
SPRING_OPTS="${SPRING_OPTS} --spring.profiles.active=prod"

# 启动应用
echo "Starting ${APP_NAME}..."
nohup java ${JVM_OPTS} -jar ${JAR_FILE} ${SPRING_OPTS} > ${LOG_DIR}/console.log 2>&1 &

# 保存PID
echo $! > ${PID_FILE}

echo "${APP_NAME} started successfully (PID: $(cat ${PID_FILE}))"
echo "Log file: ${LOG_DIR}/${APP_NAME}.log"
echo "Console log: ${LOG_DIR}/console.log"
