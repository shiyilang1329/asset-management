# 生产环境部署指南

## 📁 部署目录结构

```
/opt/asset-management/
├── backend/                        # 后端目录
│   ├── asset-management-1.0.0.jar  # 后端 JAR 包
│   ├── config/                     # 配置文件
│   │   ├── application.yml         # 应用配置
│   │   └── logback-spring.xml      # 日志配置
│   ├── logs/                       # 日志目录
│   ├── start.sh                    # 启动脚本
│   ├── stop.sh                     # 停止脚本
│   ├── restart.sh                  # 重启脚本
│   └── status.sh                   # 状态脚本
├── frontend/                       # 前端目录
│   ├── dist/                       # 前端静态文件
│   └── nginx.conf                  # Nginx 配置
├── nginx-install.sh                # Nginx 安装脚本
├── deploy-frontend.sh              # 前端部署脚本
└── database-init.sql               # 数据库脚本
```

## 🚀 快速部署

### 1. 构建部署包（开发机器）

```bash
./build-production.sh
```

### 2. 上传到服务器

```bash
scp asset-management-production-*.tar.gz root@server:/tmp/
```

### 3. 解压

```bash
mkdir -p /opt/asset-management
cd /opt/asset-management
tar -xzf /tmp/asset-management-production-*.tar.gz --strip-components=1
```

### 4. 安装 Java 17

```bash
# CentOS/RHEL
yum install java-17-openjdk -y

# Ubuntu/Debian
apt update && apt install openjdk-17-jdk -y
```

### 5. 配置后端

```bash
vi backend/config/application.yml

# 必须修改：
# 1. 确认数据库连接
# 2. 修改 JWT 密钥（重要！）
#    生成密钥: openssl rand -base64 32
```

### 6. 初始化数据库（首次部署）

```bash
mysql -h 192.168.1.119 -P 33096 -u ipi -p你的密码 zcgl < database-init.sql
```

### 7. 启动后端

```bash
cd backend
chmod +x *.sh
./start.sh
```

### 8. 部署前端

```bash
cd /opt/asset-management
chmod +x *.sh
./nginx-install.sh      # 安装 Nginx
./deploy-frontend.sh    # 部署前端
```

### 9. 访问系统

```
http://your-server-ip
账号: admin
密码: admin123
```

## 📋 服务管理

### 后端服务

```bash
cd /opt/asset-management/backend

./start.sh      # 启动
./stop.sh       # 停止
./restart.sh    # 重启
./status.sh     # 状态

# 查看日志
tail -f logs/asset-management.log
```

### 前端服务（Nginx）

```bash
systemctl start nginx       # 启动
systemctl stop nginx        # 停止
systemctl restart nginx     # 重启
systemctl status nginx      # 状态
```

## ⚙️ 配置文件

| 配置 | 路径 |
|------|------|
| 应用配置 | `/opt/asset-management/backend/config/application.yml` |
| 日志配置 | `/opt/asset-management/backend/config/logback-spring.xml` |
| Nginx配置 | `/etc/nginx/conf.d/asset-management.conf` |

## 🔍 故障排查

### 后端问题

```bash
# 查看日志
tail -f /opt/asset-management/backend/logs/console.log

# 检查端口
netstat -tlnp | grep 8080

# 测试 API
curl http://localhost:8080/api/auth/captcha
```

### 前端问题

```bash
# 检查 Nginx
systemctl status nginx
nginx -t

# 查看日志
tail -f /var/log/nginx/error.log
```

## 🔐 配置开机自启

```bash
cat > /etc/systemd/system/asset-management.service << 'EOF'
[Unit]
Description=Asset Management System
After=network.target

[Service]
Type=forking
User=root
WorkingDirectory=/opt/asset-management/backend
ExecStart=/opt/asset-management/backend/start.sh
ExecStop=/opt/asset-management/backend/stop.sh
Restart=on-failure

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable asset-management
systemctl start asset-management
```

## 📊 系统信息

- **版本**: 1.0.0
- **后端**: Spring Boot 3.2.0 + Java 17
- **前端**: Vue 3 + Nginx
- **数据库**: MySQL 8.0

---

**详细文档**: 
- [完整部署指南](PRODUCTION_DEPLOY_GUIDE.md)
- [配置说明](EXTERNAL_CONFIG_GUIDE.md)
- [部署检查清单](deploy/DEPLOY_CHECKLIST.md)
