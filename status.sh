#!/bin/bash

# ============================================
# 资产管理系统 - 服务状态检查脚本
# ============================================

echo "============================================"
echo "资产管理系统 - 服务状态"
echo "============================================"
echo ""

# 检查开发环境
echo "📊 开发环境状态："
echo "----------------------------------------"

if [ -f .backend.pid ]; then
    BACKEND_PID=$(cat .backend.pid)
    if ps -p $BACKEND_PID > /dev/null 2>&1; then
        echo "✅ 后端服务: 运行中 (PID: $BACKEND_PID)"
        echo "   端口: 8080"
        echo "   日志: logs/backend.log"
    else
        echo "❌ 后端服务: 已停止"
        rm -f .backend.pid
    fi
else
    echo "❌ 后端服务: 未运行"
fi

echo ""

if [ -f .frontend.pid ]; then
    FRONTEND_PID=$(cat .frontend.pid)
    if ps -p $FRONTEND_PID > /dev/null 2>&1; then
        echo "✅ 前端服务: 运行中 (PID: $FRONTEND_PID)"
        echo "   端口: 3000"
        echo "   日志: logs/frontend.log"
    else
        echo "❌ 前端服务: 已停止"
        rm -f .frontend.pid
    fi
else
    echo "❌ 前端服务: 未运行"
fi

echo ""
echo "----------------------------------------"
echo ""

# 检查生产环境
if command -v systemctl &> /dev/null; then
    echo "📊 生产环境状态："
    echo "----------------------------------------"
    
    if systemctl is-active --quiet asset-backend; then
        echo "✅ 后端服务: 运行中"
        systemctl status asset-backend --no-pager -l | grep "Active:"
    else
        echo "❌ 后端服务: 未运行"
    fi
    
    echo ""
    
    if systemctl is-active --quiet nginx; then
        echo "✅ Nginx服务: 运行中"
        systemctl status nginx --no-pager -l | grep "Active:"
    else
        echo "❌ Nginx服务: 未运行"
    fi
    
    echo "----------------------------------------"
    echo ""
fi

# 检查端口占用
echo "📊 端口占用情况："
echo "----------------------------------------"

if command -v lsof &> /dev/null; then
    PORT_8080=$(lsof -i :8080 -t 2>/dev/null)
    if [ ! -z "$PORT_8080" ]; then
        echo "✅ 端口 8080: 已占用 (PID: $PORT_8080)"
    else
        echo "❌ 端口 8080: 未占用"
    fi
    
    PORT_3000=$(lsof -i :3000 -t 2>/dev/null)
    if [ ! -z "$PORT_3000" ]; then
        echo "✅ 端口 3000: 已占用 (PID: $PORT_3000)"
    else
        echo "❌ 端口 3000: 未占用"
    fi
elif command -v netstat &> /dev/null; then
    if netstat -tuln | grep -q ":8080 "; then
        echo "✅ 端口 8080: 已占用"
    else
        echo "❌ 端口 8080: 未占用"
    fi
    
    if netstat -tuln | grep -q ":3000 "; then
        echo "✅ 端口 3000: 已占用"
    else
        echo "❌ 端口 3000: 未占用"
    fi
else
    echo "⚠️  无法检查端口占用（需要 lsof 或 netstat）"
fi

echo "----------------------------------------"
echo ""

# 检查数据库连接
echo "📊 数据库连接："
echo "----------------------------------------"

if command -v mysql &> /dev/null; then
    # 尝试连接数据库（需要配置）
    # mysql -h localhost -u root -p -e "SELECT 1" &> /dev/null
    echo "ℹ️  请手动检查数据库连接"
    echo "   mysql -h localhost -u root -p"
else
    echo "⚠️  MySQL客户端未安装"
fi

echo "----------------------------------------"
echo ""

echo "============================================"
echo "💡 提示："
echo "   开发环境启动: ./start-dev.sh"
echo "   开发环境停止: ./stop-dev.sh"
echo "   查看后端日志: tail -f logs/backend.log"
echo "   查看前端日志: tail -f logs/frontend.log"
echo "============================================"
