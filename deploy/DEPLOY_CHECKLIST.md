# 部署检查清单

## 部署前准备

### 1. 环境检查
- [ ] Java 17 已安装
- [ ] MySQL 8.0 已安装并运行
- [ ] 数据库已创建（zcgl）
- [ ] 数据库用户已创建并授权
- [ ] 服务器端口8080可用

### 2. 文件准备
- [ ] 复制 `asset-management-1.0.0.jar` 到部署目录
- [ ] 复制 `config/` 目录到部署目录
- [ ] 复制启动脚本到部署目录
- [ ] 设置脚本执行权限（Linux）

### 3. 配置修改
- [ ] 修改 `config/application.yml` 中的数据库配置
  - [ ] 数据库地址
  - [ ] 数据库用户名
  - [ ] 数据库密码
- [ ] 修改 JWT密钥（生产环境必须修改）
- [ ] 检查端口配置
- [ ] 检查日志配置

## 部署步骤

### Linux/Mac 部署

```bash
# 1. 创建部署目录
mkdir -p /opt/asset-management
cd /opt/asset-management

# 2. 复制文件
cp /path/to/asset-management-1.0.0.jar ./
cp -r /path/to/deploy/config ./
cp /path/to/deploy/*.sh ./

# 3. 设置权限
chmod +x *.sh

# 4. 修改配置
vi config/application.yml

# 5. 启动应用
./start.sh

# 6. 检查状态
./status.sh

# 7. 查看日志
tail -f logs/asset-management.log
```

### Windows 部署

```cmd
# 1. 创建部署目录
mkdir C:\asset-management
cd C:\asset-management

# 2. 复制文件
copy asset-management-1.0.0.jar .
xcopy /E /I config config
copy *.bat .

# 3. 修改配置
notepad config\application.yml

# 4. 启动应用
start.bat

# 5. 查看日志
type logs\asset-management.log
```

## 部署后验证

### 1. 应用启动检查
- [ ] 进程正常运行
  ```bash
  ./status.sh  # Linux
  # 或查看 app.pid 文件
  ```
- [ ] 端口正常监听
  ```bash
  netstat -tlnp | grep 8080  # Linux
  netstat -ano | findstr 8080  # Windows
  ```

### 2. 日志检查
- [ ] 应用日志无ERROR
  ```bash
  tail -n 50 logs/asset-management.log
  ```
- [ ] 数据库连接成功
  ```bash
  grep "HikariPool" logs/asset-management.log
  ```
- [ ] 应用启动成功
  ```bash
  grep "Started Application" logs/asset-management.log
  ```

### 3. 功能测试
- [ ] 访问API根路径
  ```bash
  curl http://localhost:8080/api/
  ```
- [ ] 获取验证码
  ```bash
  curl http://localhost:8080/api/auth/captcha
  ```
- [ ] 登录测试
  ```bash
  curl -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"admin123","captchaId":"xxx","captchaCode":"1234"}'
  ```

### 4. 前端部署检查
- [ ] Nginx配置正确
- [ ] 前端文件已部署
- [ ] 可以访问登录页面
- [ ] API代理配置正确

## 配置文件说明

### 必须修改的配置

#### application.yml
```yaml
# 数据库配置（必须修改）
spring:
  datasource:
    url: jdbc:mysql://YOUR_DB_HOST:3306/zcgl?...
    username: YOUR_DB_USER
    password: YOUR_DB_PASSWORD

# JWT密钥（生产环境必须修改）
jwt:
  secret: YOUR_PRODUCTION_SECRET_KEY
```

### 可选修改的配置

#### application.yml
```yaml
# 端口配置
server:
  port: 8080  # 如果8080被占用，修改为其他端口

# 文件上传大小
spring:
  servlet:
    multipart:
      max-file-size: 10MB  # 根据需要调整
      max-request-size: 50MB

# API文档（生产环境建议关闭）
knife4j:
  enable: false
```

#### logback-spring.xml
```xml
<!-- 日志路径 -->
<property name="LOG_PATH" value="./logs"/>

<!-- 日志保留天数 -->
<maxHistory>30</maxHistory>

<!-- 日志级别 -->
<root level="WARN">  <!-- 生产环境建议WARN -->
```

## 常见问题排查

### 1. 启动失败

**检查步骤**：
```bash
# 查看控制台日志
cat logs/console.log

# 查看应用日志
tail -n 100 logs/asset-management.log

# 查看错误日志
cat logs/asset-management-error.log
```

**常见原因**：
- 端口被占用
- 数据库连接失败
- 配置文件格式错误
- JVM内存不足

### 2. 数据库连接失败

**检查步骤**：
```bash
# 测试数据库连接
mysql -h HOST -P PORT -u USER -p DATABASE

# 检查配置文件
cat config/application.yml | grep datasource -A 5
```

**常见原因**：
- 数据库地址错误
- 用户名密码错误
- 数据库不存在
- 防火墙阻止连接

### 3. 内存溢出

**解决方案**：
```bash
# 修改 start.sh 中的JVM参数
JVM_OPTS="-Xms256m -Xmx512m"  # 减小内存

# 或增加服务器内存
```

### 4. 日志文件过大

**解决方案**：
```bash
# 清理旧日志
find logs/ -name "*.log.*" -mtime +7 -delete

# 或修改 logback-spring.xml 中的保留天数
<maxHistory>7</maxHistory>
```

## 监控和维护

### 日常监控
- [ ] 每天检查应用状态
- [ ] 每周检查日志文件大小
- [ ] 每月检查错误日志
- [ ] 定期备份数据库

### 监控脚本
```bash
# 创建监控脚本 monitor.sh
#!/bin/bash
./status.sh || {
    echo "Application is down, restarting..."
    ./start.sh
}
```

### 定时任务（crontab）
```bash
# 每5分钟检查一次
*/5 * * * * cd /opt/asset-management && ./monitor.sh

# 每天凌晨2点清理旧日志
0 2 * * * find /opt/asset-management/logs -name "*.log.*" -mtime +30 -delete

# 每周日凌晨3点备份数据库
0 3 * * 0 mysqldump -h host -u user -ppassword zcgl > /backup/zcgl-$(date +\%Y\%m\%d).sql
```

## 安全建议

### 1. 配置文件安全
```bash
# 设置配置文件权限
chmod 600 config/application.yml

# 只允许应用用户访问
chown app-user:app-group config/application.yml
```

### 2. JWT密钥
- 生产环境必须修改默认密钥
- 使用强密钥（至少32位随机字符）
- 定期更换密钥

### 3. 数据库安全
- 使用独立的数据库用户
- 限制数据库用户权限
- 定期备份数据库
- 使用SSL连接（可选）

### 4. 网络安全
- 配置防火墙规则
- 只开放必要端口
- 使用Nginx反向代理
- 启用HTTPS

## 回滚方案

如果部署出现问题，按以下步骤回滚：

```bash
# 1. 停止新版本
./stop.sh

# 2. 恢复旧版本jar
cp asset-management-1.0.0.jar.bak asset-management-1.0.0.jar

# 3. 恢复配置（如果有修改）
cp config/application.yml.bak config/application.yml

# 4. 启动旧版本
./start.sh

# 5. 验证
./status.sh
tail -f logs/asset-management.log
```

## 升级流程

### 1. 升级前
- [ ] 备份当前jar包
- [ ] 备份配置文件
- [ ] 备份数据库
- [ ] 通知用户系统维护

### 2. 升级中
```bash
# 停止应用
./stop.sh

# 备份
cp asset-management-1.0.0.jar asset-management-1.0.0.jar.bak
cp config/application.yml config/application.yml.bak

# 替换新版本
cp /path/to/new/asset-management-1.0.0.jar ./

# 更新配置（如果需要）
vi config/application.yml

# 启动应用
./start.sh
```

### 3. 升级后
- [ ] 检查应用状态
- [ ] 查看启动日志
- [ ] 功能测试
- [ ] 通知用户恢复使用

## 联系方式

如遇问题，请联系：
- 技术支持：开发团队
- 紧急联系：系统管理员

---

**文档版本**：1.0  
**最后更新**：2026-01-16
