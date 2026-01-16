#!/bin/bash

# ============================================
# 资产管理系统 - 开发环境启动脚本 (Linux/Mac)
# ============================================

echo "============================================"
echo "启动资产管理系统 - 开发环境"
echo "============================================"
echo ""

# 检查是否已经在运行
if [ -f .backend.pid ]; then
    BACKEND_PID=$(cat .backend.pid)
    if ps -p $BACKEND_PID > /dev/null 2>&1; then
        echo "⚠️  后端服务已在运行 (PID: $BACKEND_PID)"
    else
        rm -f .backend.pid
    fi
fi

if [ -f .frontend.pid ]; then
    FRONTEND_PID=$(cat .frontend.pid)
    if ps -p $FRONTEND_PID > /dev/null 2>&1; then
        echo "⚠️  前端服务已在运行 (PID: $FRONTEND_PID)"
    else
        rm -f .frontend.pid
    fi
fi

# 启动后端
echo "🚀 启动后端服务..."
cd backend
nohup mvn spring-boot:run > ../logs/backend.log 2>&1 &
BACKEND_PID=$!
echo $BACKEND_PID > ../.backend.pid
cd ..
echo "✅ 后端服务已启动 (PID: $BACKEND_PID)"
echo "   日志文件: logs/backend.log"
echo "   访问地址: http://localhost:8080"
echo ""

# 等待后端启动
echo "⏳ 等待后端服务启动..."
sleep 5

# 启动前端
echo "🚀 启动前端服务..."
cd frontend

# 检查是否已安装依赖
if [ ! -d "node_modules" ]; then
    echo "📦 首次运行，正在安装依赖..."
    npm install
fi

nohup npm run dev > ../logs/frontend.log 2>&1 &
FRONTEND_PID=$!
echo $FRONTEND_PID > ../.frontend.pid
cd ..
echo "✅ 前端服务已启动 (PID: $FRONTEND_PID)"
echo "   日志文件: logs/frontend.log"
echo "   访问地址: http://localhost:3000"
echo ""

echo "============================================"
echo "✅ 所有服务启动完成！"
echo "============================================"
echo ""
echo "📝 服务信息："
echo "   前端: http://localhost:3000"
echo "   后端: http://localhost:8080"
echo "   账号: admin / admin123"
echo ""
echo "📋 管理命令："
echo "   查看日志: tail -f logs/backend.log"
echo "   查看日志: tail -f logs/frontend.log"
echo "   停止服务: ./stop-dev.sh"
echo ""
echo "⏳ 等待服务完全启动（约10-15秒）..."
echo "============================================"
