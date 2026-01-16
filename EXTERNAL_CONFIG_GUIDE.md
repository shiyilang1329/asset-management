# 外部配置文件部署指南

## 概述

将配置文件放在jar包外面是生产环境的最佳实践，具有以下优势：
- ✅ 修改配置无需重新打包
- ✅ 敏感信息不打包到jar中
- ✅ 不同环境使用不同配置
- ✅ 便于配置管理和版本控制

## 配置文件清单

### 必需的配置文件（2个）

1. **config/application.yml** - 应用主配置
2. **config/logback-spring.xml** - 日志配置

## 部署目录结构

```
/opt/asset-management/          # 部署根目录
├── asset-management-1.0.0.jar  # 应用jar包
├── config/                     # 配置目录
│   ├── application.yml         # 应用配置
│   └── logback-spring.xml      # 日志配置
├── logs/                       # 日志目录（自动创建）
│   ├── asset-management.log
│   └── asset-management-error.log
├── app.pid                     # 进程ID文件
├── start.sh                    # 启动脚本
├── stop.sh                     # 停止脚本
├── restart.sh                  # 重启脚本
└── status.sh                   # 状态脚本
```

## 快速部署

### 步骤1：准备文件

```bash
# 创建部署目录
mkdir -p /opt/asset-management
cd /opt/asset-management

# 复制jar包
cp /path/to/backend/target/asset-management-1.0.0.jar ./

# 复制配置和脚本
cp -r /path/to/deploy/config ./
cp /path/to/deploy/*.sh ./

# 设置权限
chmod +x *.sh
chmod 600 config/application.yml  # 保护配置文件
```

### 步骤2：修改配置

编辑 `config/application.yml`：

```yaml
spring:
  datasource:
    # 修改数据库连接
    url: jdbc:mysql://192.168.1.119:33096/zcgl?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: ipi
    password: IPI_i314

# 修改JWT密钥（生产环境必须修改！）
jwt:
  secret: your-production-secret-key-at-least-32-characters-long
```

### 步骤3：启动应用

```bash
./start.sh
```

### 步骤4：验证部署

```bash
# 检查状态
./status.sh

# 查看日志
tail -f logs/asset-management.log

# 测试API
curl http://localhost:8080/api/auth/captcha
```

## 配置文件详解

### application.yml

```yaml
spring:
  application:
    name: asset-management
  
  # 数据源配置（必须修改）
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://HOST:PORT/DATABASE?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: YOUR_USERNAME
    password: YOUR_PASSWORD
  
  # 文件上传配置（可选修改）
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 50MB

# MyBatis Plus配置
mybatis-plus:
  configuration:
    # 生产环境关闭SQL日志
    log-impl: org.apache.ibatis.logging.nologging.NoLoggingImpl

# JWT配置（必须修改）
jwt:
  secret: YOUR_PRODUCTION_SECRET_KEY
  expiration: 86400000  # 24小时

# 服务器配置（可选修改）
server:
  port: 8080
  servlet:
    context-path: /api

# API文档配置（生产环境建议关闭）
knife4j:
  enable: false
```

### logback-spring.xml

主要配置项：

```xml
<!-- 日志路径 -->
<property name="LOG_PATH" value="./logs"/>

<!-- 滚动策略 -->
<rollingPolicy>
    <!-- 单文件最大大小 -->
    <maxFileSize>100MB</maxFileSize>
    <!-- 保留天数 -->
    <maxHistory>30</maxHistory>
    <!-- 总大小限制 -->
    <totalSizeCap>10GB</totalSizeCap>
</rollingPolicy>

<!-- 日志级别 -->
<root level="WARN">  <!-- 生产环境建议WARN -->
```

## 启动脚本说明

### start.sh

```bash
#!/bin/bash

# JVM参数
JVM_OPTS="-Xms512m -Xmx1024m"
JVM_OPTS="${JVM_OPTS} -XX:+UseG1GC"

# 指定外部配置文件
SPRING_OPTS="--spring.config.location=./config/application.yml"
SPRING_OPTS="${SPRING_OPTS} --logging.config=./config/logback-spring.xml"
SPRING_OPTS="${SPRING_OPTS} --spring.profiles.active=prod"

# 启动应用
nohup java ${JVM_OPTS} -jar asset-management-1.0.0.jar ${SPRING_OPTS} > logs/console.log 2>&1 &
```

**关键参数**：
- `--spring.config.location` - 指定配置文件位置
- `--logging.config` - 指定日志配置文件
- `--spring.profiles.active` - 激活的配置文件（prod/dev）

## 配置优先级

Spring Boot配置加载顺序（优先级从高到低）：

1. 命令行参数
2. 环境变量
3. **外部配置文件（`./config/application.yml`）** ← 推荐使用
4. jar包内配置文件

## 使用环境变量

可以通过环境变量覆盖配置：

```bash
# 方式1：在启动前设置
export DB_PASSWORD=your-password
export JWT_SECRET=your-secret-key
./start.sh

# 方式2：在start.sh中设置
# 编辑 start.sh，在启动命令前添加：
export DB_PASSWORD=your-password
export JWT_SECRET=your-secret-key
```

在application.yml中使用：
```yaml
spring:
  datasource:
    password: ${DB_PASSWORD:default-password}

jwt:
  secret: ${JWT_SECRET:default-secret}
```

## 不同环境配置

### 开发环境

```yaml
# config/application-dev.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/zcgl?...
    username: root
    password: root

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # 开启SQL日志

knife4j:
  enable: true  # 开启API文档
```

启动：
```bash
java -jar asset-management-1.0.0.jar --spring.profiles.active=dev
```

### 生产环境

```yaml
# config/application-prod.yml
spring:
  datasource:
    url: jdbc:mysql://prod-host:3306/zcgl?...
    username: prod_user
    password: ${DB_PASSWORD}

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.nologging.NoLoggingImpl  # 关闭SQL日志

knife4j:
  enable: false  # 关闭API文档
```

启动：
```bash
java -jar asset-management-1.0.0.jar --spring.profiles.active=prod
```

## 配置文件安全

### 1. 文件权限

```bash
# 配置文件只允许所有者读写
chmod 600 config/application.yml

# 日志配置可读
chmod 644 config/logback-spring.xml

# 脚本可执行
chmod +x *.sh
```

### 2. 敏感信息保护

**不推荐**：直接写在配置文件中
```yaml
spring:
  datasource:
    password: IPI_i314  # 明文密码
```

**推荐**：使用环境变量
```yaml
spring:
  datasource:
    password: ${DB_PASSWORD}  # 从环境变量读取
```

### 3. 配置文件加密（可选）

使用Jasypt加密敏感信息：

```yaml
spring:
  datasource:
    password: ENC(encrypted-password)
```

## 配置管理

### 版本控制

```bash
# 配置文件模板提交到Git
git add deploy/config/application.yml.template
git add deploy/config/logback-spring.xml

# 实际配置文件不提交
echo "config/application.yml" >> .gitignore
```

### 配置备份

```bash
# 定期备份配置
tar -czf config-backup-$(date +%Y%m%d).tar.gz config/

# 升级前备份
cp config/application.yml config/application.yml.bak
```

## 常见问题

### Q1: 配置文件没有生效？

**检查**：
```bash
# 查看启动日志，确认配置文件路径
grep "config.location" logs/console.log

# 确认文件存在
ls -la config/application.yml
```

### Q2: 如何验证使用了外部配置？

**方法**：
```bash
# 在外部配置中修改端口
server:
  port: 8081

# 重启应用
./restart.sh

# 检查新端口
netstat -tlnp | grep 8081
```

### Q3: 配置文件格式错误？

**检查**：
```yaml
# YAML格式要求：
# 1. 使用空格缩进（不能用Tab）
# 2. 冒号后必须有空格
# 3. 注意缩进层级

# 正确
spring:
  datasource:
    url: jdbc:mysql://...

# 错误（缩进不对）
spring:
datasource:
  url: jdbc:mysql://...
```

## 最佳实践

1. **配置分离**
   - 开发环境：application-dev.yml
   - 测试环境：application-test.yml
   - 生产环境：application-prod.yml

2. **敏感信息**
   - 使用环境变量
   - 不提交到版本控制
   - 定期更换密钥

3. **配置备份**
   - 升级前备份
   - 定期备份
   - 保留历史版本

4. **权限控制**
   - 限制文件访问权限
   - 只允许应用用户访问
   - 定期审计

5. **文档记录**
   - 记录配置变更
   - 说明配置用途
   - 保留配置模板

## 部署检查清单

- [ ] jar包已复制到部署目录
- [ ] config目录已创建
- [ ] application.yml已修改（数据库、JWT密钥）
- [ ] logback-spring.xml已复制
- [ ] 启动脚本已复制并设置权限
- [ ] 配置文件权限已设置（600）
- [ ] 环境变量已设置（如需要）
- [ ] 应用已启动
- [ ] 日志正常输出
- [ ] API可以访问

## 相关文档

- `deploy/README.md` - 完整部署文档
- `deploy/QUICK_DEPLOY.md` - 快速部署指南
- `deploy/DEPLOY_CHECKLIST.md` - 部署检查清单
- `deploy/FILES_SUMMARY.md` - 文件说明

---

**文档版本**：1.0  
**最后更新**：2026-01-16  
**适用版本**：asset-management 1.0.0
