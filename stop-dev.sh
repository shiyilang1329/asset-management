#!/bin/bash

# ============================================
# 资产管理系统 - 开发环境停止脚本 (Linux/Mac)
# ============================================

echo "============================================"
echo "停止资产管理系统 - 开发环境"
echo "============================================"
echo ""

# 停止后端
if [ -f .backend.pid ]; then
    BACKEND_PID=$(cat .backend.pid)
    if ps -p $BACKEND_PID > /dev/null 2>&1; then
        echo "🛑 停止后端服务 (PID: $BACKEND_PID)..."
        kill $BACKEND_PID
        
        # 等待进程结束
        for i in {1..10}; do
            if ! ps -p $BACKEND_PID > /dev/null 2>&1; then
                break
            fi
            sleep 1
        done
        
        # 如果还在运行，强制结束
        if ps -p $BACKEND_PID > /dev/null 2>&1; then
            echo "   强制停止后端服务..."
            kill -9 $BACKEND_PID
        fi
        
        rm -f .backend.pid
        echo "✅ 后端服务已停止"
    else
        echo "⚠️  后端服务未运行"
        rm -f .backend.pid
    fi
else
    echo "⚠️  未找到后端服务进程"
fi

echo ""

# 停止前端
if [ -f .frontend.pid ]; then
    FRONTEND_PID=$(cat .frontend.pid)
    if ps -p $FRONTEND_PID > /dev/null 2>&1; then
        echo "🛑 停止前端服务 (PID: $FRONTEND_PID)..."
        kill $FRONTEND_PID
        
        # 等待进程结束
        for i in {1..10}; do
            if ! ps -p $FRONTEND_PID > /dev/null 2>&1; then
                break
            fi
            sleep 1
        done
        
        # 如果还在运行，强制结束
        if ps -p $FRONTEND_PID > /dev/null 2>&1; then
            echo "   强制停止前端服务..."
            kill -9 $FRONTEND_PID
        fi
        
        rm -f .frontend.pid
        echo "✅ 前端服务已停止"
    else
        echo "⚠️  前端服务未运行"
        rm -f .frontend.pid
    fi
else
    echo "⚠️  未找到前端服务进程"
fi

echo ""

# 清理可能残留的进程
echo "🧹 清理残留进程..."

# 查找并结束 Maven 进程
MAVEN_PIDS=$(ps aux | grep '[m]vn spring-boot:run' | awk '{print $2}')
if [ ! -z "$MAVEN_PIDS" ]; then
    echo "   发现残留的Maven进程，正在清理..."
    echo "$MAVEN_PIDS" | xargs kill -9 2>/dev/null
fi

# 查找并结束 Vite 进程
VITE_PIDS=$(ps aux | grep '[n]pm run dev' | awk '{print $2}')
if [ ! -z "$VITE_PIDS" ]; then
    echo "   发现残留的Vite进程，正在清理..."
    echo "$VITE_PIDS" | xargs kill -9 2>/dev/null
fi

echo ""
echo "============================================"
echo "✅ 所有服务已停止！"
echo "============================================"
