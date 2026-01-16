# 生产环境部署完整指南

## 部署架构

```
┌─────────────────────────────────────────────────────────┐
│                      用户浏览器                          │
└────────────────────┬────────────────────────────────────┘
                     │ HTTP/HTTPS
                     ↓
┌─────────────────────────────────────────────────────────┐
│                   Nginx (端口 80/443)                    │
│  ┌──────────────────────┬──────────────────────────┐   │
│  │  静态文件服务         │   API 反向代理            │   │
│  │  /                   │   /api/*                 │   │
│  │  (前端 Vue 应用)      │   → localhost:8080       │   │
│  └──────────────────────┴──────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                                    │
                                    ↓
┌─────────────────────────────────────────────────────────┐
│          Spring Boot 应用 (端口 8080)                    │
│          asset-management-1.0.0.jar                     │
└────────────────────┬────────────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────────────┐
│              MySQL 数据库                                │
│              192.168.1.119:33096                        │
└─────────────────────────────────────────────────────────┘
```

## 一、准备工作

### 1.1 服务器要求

- **操作系统**: Linux (CentOS 7+, Ubuntu 18.04+)
- **CPU**: 2核心以上
- **内存**: 2GB 以上
- **磁盘**: 10GB 以上可用空间
- **Java**: JDK 17 或以上
- **Nginx**: 1.18 或以上
- **MySQL**: 8.0 (已有数据库服务器)

### 1.2 安装必要软件

#### 安装 Java 17

```bash
# CentOS/RHEL
sudo yum install java-17-openjdk -y

# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk -y

# 验证安装
java -version
```

#### 安装 Nginx

```bash
# CentOS/RHEL
sudo yum install nginx -y

# Ubuntu/Debian
sudo apt install nginx -y

# 启动 Nginx
sudo systemctl start nginx
sudo systemctl enable nginx

# 验证安装
nginx -v
```

### 1.3 创建部署用户（可选但推荐）

```bash
# 创建应用用户
sudo useradd -m -s /bin/bash appuser

# 切换到应用用户
sudo su - appuser
```

## 二、构建部署包

### 2.1 在开发机器上构建

```bash
# 给构建脚本执行权限
chmod +x build-production.sh

# 执行构建
./build-production.sh
```

构建完成后会生成：
- `asset-management-production-YYYYMMDD-HHMMSS.tar.gz` - 部署包

### 2.2 上传到服务器

```bash
# 上传部署包
scp asset-management-production-*.tar.gz root@your-server:/tmp/

# 或使用 SFTP、FTP 等工具上传
```

## 三、部署后端服务

### 3.1 解压部署包

```bash
# 登录服务器
ssh root@your-server

# 创建部署目录
sudo mkdir -p /opt/asset-management
cd /opt/asset-management

# 解压部署包
sudo tar -xzf /tmp/asset-management-production-*.tar.gz --strip-components=1

# 设置权限
sudo chown -R appuser:appuser /opt/asset-management
```

### 3.2 配置应用

```bash
# 编辑配置文件
cd /opt/asset-management
vi config/application.yml
```

**必须修改的配置项**：

```yaml
# 数据库配置（确认正确）
spring:
  datasource:
    url: jdbc:mysql://192.168.1.119:33096/zcgl?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: ipi
    password: IPI_i314

# JWT 密钥（必须修改为随机字符串！）
jwt:
  secret: your-production-secret-key-at-least-32-characters-long-change-this

# 服务端口（确认不冲突）
server:
  port: 8080
```

**生成安全的 JWT 密钥**：

```bash
# 生成随机密钥
openssl rand -base64 32
# 或
cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 32 | head -n 1
```

### 3.3 初始化数据库（首次部署）

```bash
# 连接数据库并执行初始化脚本
mysql -h 192.168.1.119 -P 33096 -u ipi -pIPI_i314 zcgl < database-init.sql

# 验证数据库
mysql -h 192.168.1.119 -P 33096 -u ipi -pIPI_i314 zcgl -e "SHOW TABLES;"
```

### 3.4 启动后端服务

```bash
# 给脚本执行权限
chmod +x *.sh

# 启动服务
./start.sh

# 查看启动日志
tail -f logs/asset-management.log

# 检查服务状态
./status.sh
```

### 3.5 验证后端服务

```bash
# 测试 API
curl http://localhost:8080/api/auth/captcha

# 检查端口监听
netstat -tlnp | grep 8080

# 或使用 ss
ss -tlnp | grep 8080
```

## 四、部署前端（Nginx）

### 4.1 复制前端文件

```bash
# 创建前端目录
sudo mkdir -p /usr/share/nginx/html/asset-management

# 复制前端文件
sudo cp -r /opt/asset-management/dist/* /usr/share/nginx/html/asset-management/

# 设置权限
sudo chown -R nginx:nginx /usr/share/nginx/html/asset-management
sudo chmod -R 755 /usr/share/nginx/html/asset-management
```

### 4.2 配置 Nginx

```bash
# 复制 Nginx 配置
sudo cp /opt/asset-management/nginx.conf /etc/nginx/conf.d/asset-management.conf

# 编辑配置（根据实际情况修改）
sudo vi /etc/nginx/conf.d/asset-management.conf
```

**需要修改的配置**：

```nginx
server {
    listen 80;
    server_name your-domain.com;  # 修改为你的域名或服务器IP
    
    location / {
        root /usr/share/nginx/html/asset-management;  # 确认路径正确
        index index.html;
        try_files $uri $uri/ /index.html;
    }
    
    location /api/ {
        proxy_pass http://localhost:8080/api/;  # 后端服务地址
        # ... 其他配置
    }
}
```

### 4.3 测试并重启 Nginx

```bash
# 测试配置
sudo nginx -t

# 重启 Nginx
sudo systemctl restart nginx

# 检查状态
sudo systemctl status nginx

# 查看错误日志（如有问题）
sudo tail -f /var/log/nginx/error.log
```

### 4.4 配置防火墙

```bash
# CentOS/RHEL (firewalld)
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --permanent --add-service=https
sudo firewall-cmd --reload

# Ubuntu (ufw)
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw reload

# 或直接允许 Nginx
sudo ufw allow 'Nginx Full'
```

## 五、访问系统

### 5.1 浏览器访问

```
http://your-server-ip
或
http://your-domain.com
```

### 5.2 默认登录信息

- **用户名**: admin
- **密码**: admin123

**重要**: 首次登录后立即修改管理员密码！

## 六、配置系统服务（推荐）

### 6.1 创建 Systemd 服务

```bash
sudo vi /etc/systemd/system/asset-management.service
```

添加以下内容：

```ini
[Unit]
Description=Asset Management System
After=network.target

[Service]
Type=forking
User=appuser
Group=appuser
WorkingDirectory=/opt/asset-management
ExecStart=/opt/asset-management/start.sh
ExecStop=/opt/asset-management/stop.sh
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

### 6.2 启用服务

```bash
# 重载 systemd
sudo systemctl daemon-reload

# 启动服务
sudo systemctl start asset-management

# 设置开机自启
sudo systemctl enable asset-management

# 查看状态
sudo systemctl status asset-management

# 查看日志
sudo journalctl -u asset-management -f
```

### 6.3 服务管理命令

```bash
# 启动
sudo systemctl start asset-management

# 停止
sudo systemctl stop asset-management

# 重启
sudo systemctl restart asset-management

# 查看状态
sudo systemctl status asset-management

# 查看日志
sudo journalctl -u asset-management -n 100
```

## 七、日志管理

### 7.1 应用日志

```bash
# 实时查看应用日志
tail -f /opt/asset-management/logs/asset-management.log

# 查看错误日志
tail -f /opt/asset-management/logs/asset-management-error.log

# 查看控制台日志
tail -f /opt/asset-management/logs/console.log
```

### 7.2 Nginx 日志

```bash
# 访问日志
tail -f /var/log/nginx/asset-management-access.log

# 错误日志
tail -f /var/log/nginx/asset-management-error.log
```

### 7.3 日志轮转配置

```bash
# 创建日志轮转配置
sudo vi /etc/logrotate.d/asset-management
```

添加：

```
/opt/asset-management/logs/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    create 0644 appuser appuser
    sharedscripts
    postrotate
        /opt/asset-management/restart.sh > /dev/null 2>&1 || true
    endscript
}
```

## 八、备份策略

### 8.1 数据库备份

```bash
# 创建备份脚本
vi /opt/backup/backup-database.sh
```

```bash
#!/bin/bash
BACKUP_DIR="/opt/backup/database"
DATE=$(date +%Y%m%d-%H%M%S)
mkdir -p ${BACKUP_DIR}

mysqldump -h 192.168.1.119 -P 33096 -u ipi -pIPI_i314 zcgl \
  > ${BACKUP_DIR}/zcgl-${DATE}.sql

# 压缩备份
gzip ${BACKUP_DIR}/zcgl-${DATE}.sql

# 删除30天前的备份
find ${BACKUP_DIR} -name "*.sql.gz" -mtime +30 -delete

echo "Database backup completed: zcgl-${DATE}.sql.gz"
```

```bash
# 设置权限
chmod +x /opt/backup/backup-database.sh

# 添加到 crontab（每天凌晨2点备份）
crontab -e
```

添加：
```
0 2 * * * /opt/backup/backup-database.sh >> /opt/backup/backup.log 2>&1
```

### 8.2 配置文件备份

```bash
# 备份配置
tar -czf /opt/backup/config-$(date +%Y%m%d).tar.gz \
  /opt/asset-management/config/
```

## 九、监控和维护

### 9.1 健康检查脚本

```bash
vi /opt/asset-management/health-check.sh
```

```bash
#!/bin/bash

# 检查后端服务
if curl -s http://localhost:8080/api/auth/captcha > /dev/null; then
    echo "✓ Backend service is running"
else
    echo "✗ Backend service is down"
    # 发送告警或重启服务
    systemctl restart asset-management
fi

# 检查 Nginx
if systemctl is-active --quiet nginx; then
    echo "✓ Nginx is running"
else
    echo "✗ Nginx is down"
    systemctl restart nginx
fi
```

### 9.2 性能监控

```bash
# 查看 Java 进程资源使用
ps aux | grep asset-management

# 查看内存使用
free -h

# 查看磁盘使用
df -h

# 查看网络连接
netstat -an | grep 8080
```

## 十、故障排查

### 10.1 后端服务无法启动

```bash
# 检查日志
tail -f /opt/asset-management/logs/console.log

# 检查端口占用
netstat -tlnp | grep 8080

# 检查 Java 版本
java -version

# 检查配置文件
cat /opt/asset-management/config/application.yml
```

### 10.2 前端无法访问

```bash
# 检查 Nginx 状态
systemctl status nginx

# 检查 Nginx 配置
nginx -t

# 检查文件权限
ls -la /usr/share/nginx/html/asset-management/

# 查看 Nginx 错误日志
tail -f /var/log/nginx/error.log
```

### 10.3 API 请求失败

```bash
# 测试后端直接访问
curl http://localhost:8080/api/auth/captcha

# 测试通过 Nginx 访问
curl http://localhost/api/auth/captcha

# 检查防火墙
sudo firewall-cmd --list-all
```

### 10.4 数据库连接失败

```bash
# 测试数据库连接
mysql -h 192.168.1.119 -P 33096 -u ipi -pIPI_i314 zcgl -e "SELECT 1;"

# 检查网络连通性
ping 192.168.1.119
telnet 192.168.1.119 33096
```

## 十一、安全加固

### 11.1 修改默认密码

1. 登录系统
2. 进入用户管理
3. 修改 admin 用户密码

### 11.2 配置 HTTPS（推荐）

```bash
# 安装 Certbot（Let's Encrypt）
sudo yum install certbot python3-certbot-nginx -y

# 获取证书
sudo certbot --nginx -d your-domain.com

# 自动续期
sudo certbot renew --dry-run
```

### 11.3 限制访问

```nginx
# 在 Nginx 配置中添加 IP 白名单
location /api/ {
    allow 192.168.1.0/24;
    deny all;
    proxy_pass http://localhost:8080/api/;
}
```

### 11.4 配置文件权限

```bash
# 限制配置文件访问
chmod 600 /opt/asset-management/config/application.yml
chown appuser:appuser /opt/asset-management/config/application.yml
```

## 十二、升级部署

### 12.1 备份当前版本

```bash
# 备份数据库
mysqldump -h 192.168.1.119 -P 33096 -u ipi -pIPI_i314 zcgl > backup.sql

# 备份配置
cp -r /opt/asset-management/config /opt/backup/config-backup

# 备份 JAR
cp /opt/asset-management/asset-management-1.0.0.jar /opt/backup/
```

### 12.2 部署新版本

```bash
# 停止服务
sudo systemctl stop asset-management

# 替换 JAR 文件
cp new-version.jar /opt/asset-management/asset-management-1.0.0.jar

# 更新前端文件
rm -rf /usr/share/nginx/html/asset-management/*
cp -r new-dist/* /usr/share/nginx/html/asset-management/

# 启动服务
sudo systemctl start asset-management

# 验证
./status.sh
```

## 十三、性能优化

### 13.1 JVM 参数调优

编辑 `start.sh`：

```bash
# 根据服务器内存调整
JVM_OPTS="-Xms1g -Xmx2g"
JVM_OPTS="${JVM_OPTS} -XX:+UseG1GC"
JVM_OPTS="${JVM_OPTS} -XX:MaxGCPauseMillis=200"
JVM_OPTS="${JVM_OPTS} -XX:+HeapDumpOnOutOfMemoryError"
```

### 13.2 Nginx 优化

```nginx
# 启用 Gzip
gzip on;
gzip_types text/plain text/css application/json application/javascript;

# 启用缓存
location ~* \.(js|css|png|jpg|jpeg|gif|ico)$ {
    expires 30d;
    add_header Cache-Control "public, immutable";
}

# 连接优化
keepalive_timeout 65;
client_max_body_size 50M;
```

## 十四、部署检查清单

- [ ] Java 17 已安装
- [ ] Nginx 已安装并运行
- [ ] 数据库连接正常
- [ ] 部署包已上传并解压
- [ ] 配置文件已修改（数据库、JWT密钥）
- [ ] 数据库已初始化
- [ ] 后端服务已启动
- [ ] 前端文件已部署到 Nginx
- [ ] Nginx 配置已更新
- [ ] 防火墙规则已配置
- [ ] 系统可以正常访问
- [ ] 默认密码已修改
- [ ] 备份策略已配置
- [ ] 日志轮转已配置
- [ ] 监控脚本已部署

## 十五、常用命令速查

```bash
# 服务管理
systemctl start asset-management    # 启动
systemctl stop asset-management     # 停止
systemctl restart asset-management  # 重启
systemctl status asset-management   # 状态

# 日志查看
tail -f logs/asset-management.log   # 应用日志
journalctl -u asset-management -f   # 系统日志

# Nginx 管理
systemctl restart nginx             # 重启 Nginx
nginx -t                            # 测试配置
tail -f /var/log/nginx/error.log   # 错误日志

# 进程管理
ps aux | grep asset-management      # 查看进程
netstat -tlnp | grep 8080          # 查看端口

# 数据库
mysql -h 192.168.1.119 -P 33096 -u ipi -pIPI_i314 zcgl
```

---

**部署支持**：如有问题，请查看日志文件或联系技术支持。
