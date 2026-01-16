#!/bin/bash

# Nginx 安装和配置脚本
# 适用于 CentOS/RHEL 和 Ubuntu/Debian

set -e

echo "=========================================="
echo "Nginx 安装和配置脚本"
echo "=========================================="

# 检测操作系统
if [ -f /etc/redhat-release ]; then
    OS="centos"
    echo "检测到 CentOS/RHEL 系统"
elif [ -f /etc/lsb-release ]; then
    OS="ubuntu"
    echo "检测到 Ubuntu/Debian 系统"
else
    echo "不支持的操作系统"
    exit 1
fi

# 安装 Nginx
echo ""
echo "1. 安装 Nginx..."
if [ "$OS" = "centos" ]; then
    sudo yum install -y nginx
elif [ "$OS" = "ubuntu" ]; then
    sudo apt update
    sudo apt install -y nginx
fi

# 启动 Nginx
echo ""
echo "2. 启动 Nginx..."
sudo systemctl start nginx
sudo systemctl enable nginx

# 检查状态
echo ""
echo "3. 检查 Nginx 状态..."
sudo systemctl status nginx --no-pager

# 创建前端目录
echo ""
echo "4. 创建前端目录..."
sudo mkdir -p /usr/share/nginx/html/asset-management
echo "✓ 目录已创建: /usr/share/nginx/html/asset-management"

# 配置防火墙
echo ""
echo "5. 配置防火墙..."
if [ "$OS" = "centos" ]; then
    if command -v firewall-cmd &> /dev/null; then
        sudo firewall-cmd --permanent --add-service=http
        sudo firewall-cmd --permanent --add-service=https
        sudo firewall-cmd --reload
        echo "✓ 防火墙规则已添加"
    fi
elif [ "$OS" = "ubuntu" ]; then
    if command -v ufw &> /dev/null; then
        sudo ufw allow 'Nginx Full'
        echo "✓ 防火墙规则已添加"
    fi
fi

# 显示 Nginx 版本
echo ""
echo "=========================================="
echo "Nginx 安装完成！"
echo "=========================================="
nginx -v

echo ""
echo "下一步操作："
echo "1. 复制前端文件到: /usr/share/nginx/html/asset-management/"
echo "2. 配置 Nginx: /etc/nginx/conf.d/asset-management.conf"
echo "3. 测试配置: nginx -t"
echo "4. 重启 Nginx: systemctl restart nginx"
