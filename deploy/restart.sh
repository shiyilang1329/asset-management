#!/bin/bash

# 资产管理系统重启脚本

echo "Restarting asset-management..."

# 停止应用
./stop.sh

# 等待2秒
sleep 2

# 启动应用
./start.sh
