#!/bin/bash

# 前端部署脚本
# 用于将前端文件部署到 Nginx

set -e

echo "=========================================="
echo "前端部署脚本"
echo "=========================================="

# 配置变量
FRONTEND_DIR="./frontend/dist"
NGINX_DIR="/usr/share/nginx/html/asset-management"
NGINX_CONF_SOURCE="./frontend/nginx.conf"
NGINX_CONF_TARGET="/etc/nginx/conf.d/asset-management.conf"

# 检查前端文件
if [ ! -d "$FRONTEND_DIR" ]; then
    echo "错误: 前端文件目录不存在: $FRONTEND_DIR"
    echo "请确保在部署目录中执行此脚本"
    exit 1
fi

# 检查 Nginx 是否安装
if ! command -v nginx &> /dev/null; then
    echo "错误: Nginx 未安装"
    echo "请先运行: ./nginx-install.sh"
    exit 1
fi

# 1. 创建目标目录
echo ""
echo "1. 创建 Nginx 目录..."
sudo mkdir -p $NGINX_DIR
echo "✓ 目录已创建: $NGINX_DIR"

# 2. 备份旧文件（如果存在）
if [ -d "$NGINX_DIR" ] && [ "$(ls -A $NGINX_DIR)" ]; then
    echo ""
    echo "2. 备份旧文件..."
    BACKUP_DIR="/opt/backup/frontend-$(date +%Y%m%d-%H%M%S)"
    sudo mkdir -p $BACKUP_DIR
    sudo cp -r $NGINX_DIR/* $BACKUP_DIR/
    echo "✓ 旧文件已备份到: $BACKUP_DIR"
fi

# 3. 复制前端文件
echo ""
echo "3. 复制前端文件..."
sudo rm -rf $NGINX_DIR/*
sudo cp -r $FRONTEND_DIR/* $NGINX_DIR/
echo "✓ 前端文件已复制"

# 4. 设置权限
echo ""
echo "4. 设置文件权限..."
sudo chown -R nginx:nginx $NGINX_DIR
sudo chmod -R 755 $NGINX_DIR
echo "✓ 权限已设置"

# 5. 配置 Nginx
echo ""
echo "5. 配置 Nginx..."
if [ -f "$NGINX_CONF_SOURCE" ]; then
    # 备份旧配置
    if [ -f "$NGINX_CONF_TARGET" ]; then
        sudo cp $NGINX_CONF_TARGET ${NGINX_CONF_TARGET}.bak
        echo "✓ 旧配置已备份: ${NGINX_CONF_TARGET}.bak"
    fi
    
    # 复制新配置
    sudo cp $NGINX_CONF_SOURCE $NGINX_CONF_TARGET
    echo "✓ Nginx 配置已更新"
    
    # 提示修改配置
    echo ""
    echo "⚠ 请检查并修改 Nginx 配置:"
    echo "   sudo vi $NGINX_CONF_TARGET"
    echo ""
    echo "需要修改的配置项:"
    echo "  - server_name: 修改为你的域名或IP"
    echo "  - root: 确认前端文件路径正确"
    echo "  - proxy_pass: 确认后端服务地址正确"
else
    echo "⚠ 未找到 nginx.conf 模板文件"
    echo "请手动创建配置文件: $NGINX_CONF_TARGET"
fi

# 6. 测试 Nginx 配置
echo ""
echo "6. 测试 Nginx 配置..."
if sudo nginx -t; then
    echo "✓ Nginx 配置测试通过"
else
    echo "✗ Nginx 配置测试失败"
    echo "请检查配置文件: $NGINX_CONF_TARGET"
    exit 1
fi

# 7. 重启 Nginx
echo ""
echo "7. 重启 Nginx..."
sudo systemctl restart nginx
echo "✓ Nginx 已重启"

# 8. 检查状态
echo ""
echo "8. 检查 Nginx 状态..."
sudo systemctl status nginx --no-pager | head -n 10

# 显示部署信息
echo ""
echo "=========================================="
echo "前端部署完成！"
echo "=========================================="
echo "前端文件位置: $NGINX_DIR"
echo "Nginx 配置: $NGINX_CONF_TARGET"
echo ""
echo "访问地址:"
echo "  http://$(hostname -I | awk '{print $1}')"
echo "  或"
echo "  http://your-domain.com"
echo ""
echo "查看日志:"
echo "  sudo tail -f /var/log/nginx/asset-management-access.log"
echo "  sudo tail -f /var/log/nginx/asset-management-error.log"
echo "=========================================="
