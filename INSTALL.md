# 资产管理系统 - 安装部署指南

## 📋 环境要求

### 必需环境
- **JDK**: 17 或更高版本
- **Node.js**: 16 或更高版本
- **MySQL**: 8.0 或更高版本
- **Maven**: 3.6 或更高版本

### 可选环境
- 无

## 🚀 快速安装（Windows）

### 1. 数据库初始化

```bash
# 登录MySQL
mysql -u root -p

# 执行初始化脚本
source database-init.sql
# 或
mysql -u root -p < database-init.sql
```

### 2. 配置数据库连接

编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/zcgl?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

### 3. 启动服务

```bash
# 方式1：使用启动脚本（推荐）
start-dev.bat

# 方式2：手动启动
# 启动后端
cd backend
mvn spring-boot:run

# 启动前端（新开命令行窗口）
cd frontend
npm install
npm run dev
```

### 4. 访问系统

- 前端地址：http://localhost:3000
- 后端地址：http://localhost:8080
- 默认账号：admin / admin123

### 5. 停止服务

```bash
stop-dev.bat
```

## 🐧 Linux/Mac 部署

### 1. 数据库初始化

```bash
mysql -u root -p < database-init.sql
```

### 2. 配置数据库连接

编辑 `backend/src/main/resources/application.yml`

### 3. 启动后端

```bash
cd backend
mvn clean package -DskipTests
java -jar target/asset-management-1.0.0.jar
```

### 4. 启动前端

```bash
cd frontend
npm install
npm run build
npm run preview
# 或使用 nginx 部署 dist 目录
```

## 📦 生产环境部署

### 后端部署

```bash
cd backend
mvn clean package -DskipTests

# 运行jar包
java -jar target/asset-management-1.0.0.jar --spring.profiles.active=prod
```

### 前端部署

```bash
cd frontend
npm install
npm run build

# 将 dist 目录部署到 nginx
```

### Nginx 配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端
    location / {
        root /path/to/frontend/dist;
        try_files $uri $uri/ /index.html;
    }

    # 后端API代理
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 🔧 配置说明

### 数据库配置

`backend/src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/zcgl
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 端口配置

```yaml
server:
  port: 8080  # 后端端口
```

前端端口在 `frontend/vite.config.ts` 中配置：

```typescript
export default defineConfig({
  server: {
    port: 3000  // 前端端口
  }
})
```

### JWT 配置

```yaml
jwt:
  secret: your-secret-key
  expiration: 86400000  # 24小时
```

## 📝 初始化数据

系统会自动创建：
- 默认管理员账号：admin / admin123
- 3个预设角色：ADMIN、MANAGER、USER
- 40个权限（8个菜单 + 32个按钮）
- 8个资产分类
- 5个示例资产

## 🐛 常见问题

### 1. 端口被占用

```bash
# Windows 查看端口占用
netstat -ano | findstr :8080
netstat -ano | findstr :3000

# 结束进程
taskkill /PID <进程ID> /F
```

### 2. 数据库连接失败

- 检查MySQL服务是否启动
- 检查数据库用户名密码是否正确
- 检查数据库是否已创建

### 3. 前端无法访问后端

- 检查后端是否启动成功
- 检查端口配置是否正确
- 检查防火墙设置

### 4. Maven 依赖下载慢

配置国内镜像源，编辑 `~/.m2/settings.xml`：

```xml
<mirrors>
  <mirror>
    <id>aliyun</id>
    <mirrorOf>central</mirrorOf>
    <url>https://maven.aliyun.com/repository/public</url>
  </mirror>
</mirrors>
```

### 5. npm 安装慢

```bash
# 使用淘宝镜像
npm config set registry https://registry.npmmirror.com
```

## 📞 技术支持

如遇问题，请查看：
1. 后端日志：`backend/logs/asset-management.log`
2. 浏览器控制台
3. 数据库连接状态

## 🔄 更新升级

### 更新代码

```bash
git pull origin main
```

### 更新数据库

```bash
# 执行增量SQL脚本
mysql -u root -p zcgl < update-xxx.sql
```

### 重新部署

```bash
# 后端
cd backend
mvn clean package -DskipTests

# 前端
cd frontend
npm run build
```

---

**版本**: v1.3.0  
**更新日期**: 2026-01-15
