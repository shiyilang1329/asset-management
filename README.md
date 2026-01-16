# 资产管理系统

企业级资产管理系统，支持资产全生命周期管理。

## 技术栈

- **后端**: Spring Boot 3.2.0 + MyBatis Plus + MySQL
- **前端**: Vue 3 + Element Plus + TypeScript
- **安全**: Spring Security + JWT
- **部署**: Nginx + JAR (Linux)

## 功能特性

- ✅ 资产全生命周期管理
- ✅ 资产领用与归还
- ✅ 资产维修记录
- ✅ 资产报废处理
- ✅ 人员管理
- ✅ 部门管理
- ✅ 权限管理（RBAC）
- ✅ 数据统计与报表
- ✅ Excel 导入导出
- ✅ 登录安全（验证码、频率限制）

## 快速开始

### 开发环境

```bash
# 启动后端和前端（Linux/Mac）
./start-dev.sh

# 访问系统
http://localhost:3000
```

### 生产部署

```bash
# 1. 构建部署包
./build-production.sh

# 2. 上传到服务器
scp asset-management-production-*.tar.gz root@server:/tmp/

# 3. 在服务器上部署
# 详见 DEPLOY_GUIDE.md
```

## 文档

- **[部署指南](DEPLOY_GUIDE.md)** - 生产环境部署
- **[完整部署文档](PRODUCTION_DEPLOY_GUIDE.md)** - 详细部署步骤
- **[配置说明](EXTERNAL_CONFIG_GUIDE.md)** - 配置文件详解
- **[安全说明](SECURITY.md)** - 安全功能说明
- **[安装指南](INSTALL.md)** - 开发环境安装

## 系统要求

### 开发环境

- JDK 17+
- Maven 3.6+
- Node.js 16+
- MySQL 8.0+

### 生产环境（Linux）

- Linux 服务器（CentOS 7+, Ubuntu 18.04+）
- JDK 17+
- Nginx 1.18+
- MySQL 8.0+

## 默认账号

- **用户名**: admin
- **密码**: admin123

⚠️ **重要**: 首次登录后请立即修改密码！

## 数据库配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://192.168.1.119:33096/zcgl
    username: xxx
    password: XXXXXX
```

## 端口配置

- **后端**: 8080
- **前端开发**: 3000
- **前端生产**: 80 (Nginx)

## 项目结构

```
.
├── backend/                    # 后端代码
│   ├── src/main/java/         # Java 源码
│   ├── src/main/resources/    # 配置文件
│   └── pom.xml                # Maven 配置
├── frontend/                   # 前端代码
│   ├── src/                   # 源码
│   └── package.json           # NPM 配置
├── deploy/                     # 部署脚本
│   ├── config/                # 生产配置
│   └── *.sh                   # 启动脚本
├── build-production.sh         # 构建脚本
├── database-init.sql           # 数据库初始化
└── README.md                   # 本文件
```

## 安全特性

- ✅ JWT Token 认证
- ✅ 密码加密存储（BCrypt）
- ✅ 登录验证码
- ✅ 登录频率限制（5次失败锁定15分钟）
- ✅ 权限控制（RBAC）

## 浏览器支持

- Chrome (推荐)
- Firefox
- Safari
- Edge

## 响应式设计

- ✅ 桌面端（1920x1080+）
- ✅ 平板端（768px-1024px）
- ✅ 手机端（<768px）

---

**版本**: 1.0.0  
**更新时间**: 2026-01-16
