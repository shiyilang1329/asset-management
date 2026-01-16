# 部署目录说明

## 目录结构

```
deploy/
├── config/                          # 配置文件目录
│   ├── application.yml              # 应用配置
│   └── logback-spring.xml           # 日志配置
├── logs/                            # 日志目录（自动创建）
│   ├── asset-management.log         # 应用日志
│   ├── asset-management-error.log   # 错误日志
│   └── console.log                  # 控制台输出
├── asset-management-1.0.0.jar       # 应用jar包
├── app.pid                          # 进程ID文件（运行时生成）
├── start.sh                         # 启动脚本
├── stop.sh                          # 停止脚本
├── restart.sh                       # 重启脚本
├── status.sh                        # 状态查看脚本
└── README.md                        # 本文件
```

## 部署步骤

### 1. 准备部署文件

将以下文件复制到服务器的部署目录：

```bash
# 创建部署目录
mkdir -p /opt/asset-management
cd /opt/asset-management

# 复制文件
cp backend/target/asset-management-1.0.0.jar ./
cp -r deploy/config ./
cp deploy/*.sh ./
```

### 2. 修改配置文件

编辑 `config/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://your-db-host:3306/zcgl?...
    username: your-username
    password: your-password

jwt:
  secret: your-production-secret-key  # 修改为生产环境密钥
```

### 3. 设置脚本权限

```bash
chmod +x *.sh
```

### 4. 启动应用

```bash
./start.sh
```

## 脚本使用

### 启动应用
```bash
./start.sh
```

输出示例：
```
Starting asset-management...
asset-management started successfully (PID: 12345)
Log file: ./logs/asset-management.log
Console log: ./logs/console.log
```

### 停止应用
```bash
./stop.sh
```

输出示例：
```
Stopping asset-management (PID: 12345)...
asset-management stopped successfully
```

### 重启应用
```bash
./restart.sh
```

### 查看状态
```bash
./status.sh
```

输出示例：
```
asset-management is running (PID: 12345)

Process info:
  PID  PPID CMD                         %MEM %CPU     ELAPSED
12345     1 java -Xms512m -Xmx1024m...  15.2  2.5    01:23:45

Listening ports:
tcp        0      0 0.0.0.0:8080            0.0.0.0:*               LISTEN      12345/java
```

### 查看日志
```bash
# 实时查看应用日志
tail -f logs/asset-management.log

# 实时查看错误日志
tail -f logs/asset-management-error.log

# 实时查看控制台输出
tail -f logs/console.log

# 查看最近100行日志
tail -n 100 logs/asset-management.log
```

## 配置说明

### application.yml

主要配置项：

1. **数据库配置**
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://host:port/database
       username: user
       password: pass
   ```

2. **JWT配置**
   ```yaml
   jwt:
     secret: your-secret-key
     expiration: 86400000  # 24小时
   ```

3. **服务器配置**
   ```yaml
   server:
     port: 8080
     servlet:
       context-path: /api
   ```

4. **文件上传**
   ```yaml
   spring:
     servlet:
       multipart:
         max-file-size: 10MB
         max-request-size: 50MB
   ```

### logback-spring.xml

日志配置：

1. **日志路径**：`./logs/`
2. **日志文件**：
   - `asset-management.log` - 所有日志
   - `asset-management-error.log` - 错误日志
3. **滚动策略**：
   - 单文件最大：100MB
   - 保留天数：30天
   - 总大小限制：10GB
4. **日志级别**：
   - 生产环境：WARN
   - 开发环境：INFO

## JVM参数说明

在 `start.sh` 中配置的JVM参数：

```bash
JVM_OPTS="-Xms512m -Xmx1024m"              # 堆内存：初始512M，最大1G
JVM_OPTS="${JVM_OPTS} -XX:+UseG1GC"        # 使用G1垃圾回收器
JVM_OPTS="${JVM_OPTS} -XX:MaxGCPauseMillis=200"  # GC暂停时间目标
JVM_OPTS="${JVM_OPTS} -XX:+HeapDumpOnOutOfMemoryError"  # OOM时生成堆转储
```

根据服务器配置调整：
- 小型服务器（2G内存）：`-Xms256m -Xmx512m`
- 中型服务器（4G内存）：`-Xms512m -Xmx1024m`
- 大型服务器（8G+内存）：`-Xms1024m -Xmx2048m`

## 环境变量

可以通过环境变量覆盖配置：

```bash
# 设置数据库密码
export DB_PASSWORD=your-password

# 设置JWT密钥
export JWT_SECRET=your-secret-key

# 启动应用
./start.sh
```

## 系统服务配置（可选）

创建systemd服务文件 `/etc/systemd/system/asset-management.service`：

```ini
[Unit]
Description=Asset Management System
After=network.target

[Service]
Type=forking
User=your-user
WorkingDirectory=/opt/asset-management
ExecStart=/opt/asset-management/start.sh
ExecStop=/opt/asset-management/stop.sh
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

使用systemd管理：
```bash
# 重新加载配置
sudo systemctl daemon-reload

# 启动服务
sudo systemctl start asset-management

# 停止服务
sudo systemctl stop asset-management

# 重启服务
sudo systemctl restart asset-management

# 查看状态
sudo systemctl status asset-management

# 开机自启
sudo systemctl enable asset-management
```

## 常见问题

### 1. 端口被占用
```bash
# 查看8080端口占用
lsof -i:8080
# 或
netstat -tlnp | grep 8080

# 修改端口（在application.yml中）
server:
  port: 8081
```

### 2. 内存不足
```bash
# 减小JVM堆内存（在start.sh中）
JVM_OPTS="-Xms256m -Xmx512m"
```

### 3. 数据库连接失败
```bash
# 检查数据库配置
cat config/application.yml

# 测试数据库连接
mysql -h host -P port -u user -p database
```

### 4. 日志文件过大
```bash
# 清理旧日志
find logs/ -name "*.log.*" -mtime +30 -delete

# 或手动删除
rm logs/asset-management-2024-*.log
```

## 监控建议

1. **应用监控**
   - 定期检查 `status.sh`
   - 监控日志文件大小
   - 关注错误日志

2. **资源监控**
   ```bash
   # CPU和内存使用
   top -p $(cat app.pid)
   
   # 磁盘使用
   df -h
   
   # 日志目录大小
   du -sh logs/
   ```

3. **健康检查**
   ```bash
   # 检查应用是否响应
   curl http://localhost:8080/api/
   ```

## 备份建议

定期备份：
1. 配置文件：`config/`
2. 日志文件：`logs/`（可选）
3. 数据库：使用mysqldump

```bash
# 备份配置
tar -czf config-backup-$(date +%Y%m%d).tar.gz config/

# 备份数据库
mysqldump -h host -u user -p database > backup-$(date +%Y%m%d).sql
```

## 升级步骤

1. 停止应用：`./stop.sh`
2. 备份当前jar：`cp asset-management-1.0.0.jar asset-management-1.0.0.jar.bak`
3. 替换新jar包
4. 启动应用：`./start.sh`
5. 检查日志：`tail -f logs/asset-management.log`

---

**维护人员**：开发团队  
**最后更新**：2026-01-16
