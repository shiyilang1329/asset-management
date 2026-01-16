#!/bin/bash

# 生产环境构建脚本
# 用途：在开发机器上构建后端和前端，生成部署包

set -e

echo "=========================================="
echo "开始构建生产环境部署包"
echo "=========================================="

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 检查必要的工具
echo -e "${YELLOW}检查构建工具...${NC}"
command -v mvn >/dev/null 2>&1 || { echo -e "${RED}错误: 需要安装 Maven${NC}" >&2; exit 1; }
command -v npm >/dev/null 2>&1 || { echo -e "${RED}错误: 需要安装 Node.js 和 npm${NC}" >&2; exit 1; }

# 创建部署目录
DEPLOY_DIR="production-deploy"
rm -rf ${DEPLOY_DIR}
mkdir -p ${DEPLOY_DIR}

echo -e "${GREEN}✓ 创建部署目录: ${DEPLOY_DIR}${NC}"

# 1. 构建后端
echo ""
echo "=========================================="
echo "1. 构建后端 JAR 包"
echo "=========================================="
cd backend
mvn clean package -DskipTests
cd ..

# 创建后端目录结构
mkdir -p ${DEPLOY_DIR}/backend/config
mkdir -p ${DEPLOY_DIR}/backend/logs

# 复制后端 JAR
cp backend/target/asset-management-1.0.0.jar ${DEPLOY_DIR}/backend/
echo -e "${GREEN}✓ 后端 JAR 包已复制${NC}"

# 复制后端配置文件
cp deploy/config/application.yml ${DEPLOY_DIR}/backend/config/
cp deploy/config/logback-spring.xml ${DEPLOY_DIR}/backend/config/
echo -e "${GREEN}✓ 后端配置文件已复制${NC}"

# 复制后端启动脚本
cp deploy/start.sh ${DEPLOY_DIR}/backend/
cp deploy/stop.sh ${DEPLOY_DIR}/backend/
cp deploy/restart.sh ${DEPLOY_DIR}/backend/
cp deploy/status.sh ${DEPLOY_DIR}/backend/
chmod +x ${DEPLOY_DIR}/backend/*.sh
echo -e "${GREEN}✓ 后端启动脚本已复制${NC}"

# 2. 构建前端
echo ""
echo "=========================================="
echo "2. 构建前端静态文件"
echo "=========================================="
cd frontend
npm install
npm run build
cd ..

# 创建前端目录
mkdir -p ${DEPLOY_DIR}/frontend

# 复制前端 dist
cp -r frontend/dist ${DEPLOY_DIR}/frontend/
echo -e "${GREEN}✓ 前端静态文件已复制${NC}"

# 复制 nginx 配置
cp frontend/nginx.conf ${DEPLOY_DIR}/frontend/
echo -e "${GREEN}✓ Nginx 配置已复制${NC}"

# 3. 复制部署脚本和文档
echo ""
echo "=========================================="
echo "3. 复制部署脚本和文档"
echo "=========================================="

# 复制部署脚本
cp deploy/nginx-install.sh ${DEPLOY_DIR}/
cp deploy/deploy-frontend.sh ${DEPLOY_DIR}/
chmod +x ${DEPLOY_DIR}/*.sh
echo -e "${GREEN}✓ 部署脚本已复制${NC}"

# 复制文档
cp deploy/README.md ${DEPLOY_DIR}/DEPLOY_README.md
cp deploy/QUICK_DEPLOY.md ${DEPLOY_DIR}/
cp deploy/DEPLOY_CHECKLIST.md ${DEPLOY_DIR}/
cp EXTERNAL_CONFIG_GUIDE.md ${DEPLOY_DIR}/
cp PRODUCTION_DEPLOY_GUIDE.md ${DEPLOY_DIR}/
cp QUICK_START.md ${DEPLOY_DIR}/
echo -e "${GREEN}✓ 部署文档已复制${NC}"

# 复制数据库初始化脚本
cp database-init.sql ${DEPLOY_DIR}/
echo -e "${GREEN}✓ 数据库脚本已复制${NC}"

# 4. 生成部署说明
echo ""
echo "=========================================="
echo "4. 生成部署说明"
echo "=========================================="

cat > ${DEPLOY_DIR}/DEPLOY_INSTRUCTIONS.txt << 'EOF'
========================================
资产管理系统 - 生产环境部署说明
========================================

一、部署包内容
--------------
backend/                        # 后端目录
  ├── asset-management-1.0.0.jar  - 后端应用
  ├── config/                     - 配置文件目录
  │   ├── application.yml         - 应用配置
  │   └── logback-spring.xml      - 日志配置
  ├── logs/                       - 日志目录（自动创建）
  ├── start.sh                    - 启动脚本
  ├── stop.sh                     - 停止脚本
  ├── restart.sh                  - 重启脚本
  └── status.sh                   - 状态检查脚本
frontend/                       # 前端目录
  ├── dist/                       - 前端静态文件
  └── nginx.conf                  - Nginx 配置示例
nginx-install.sh                - Nginx 安装脚本
deploy-frontend.sh              - 前端部署脚本
database-init.sql               - 数据库初始化脚本
README.md                       - 详细部署文档
QUICK_DEPLOY.md                 - 快速部署指南
DEPLOY_CHECKLIST.md             - 部署检查清单
EXTERNAL_CONFIG_GUIDE.md        - 外部配置指南

二、快速部署步骤
--------------

1. 上传部署包到服务器
   scp -r production-deploy root@your-server:/opt/asset-management

2. 登录服务器
   ssh root@your-server

3. 进入部署目录
   cd /opt/asset-management

4. 修改后端配置文件
   vi backend/config/application.yml
   # 修改数据库连接信息
   # 修改 JWT 密钥（重要！）

5. 初始化数据库（首次部署）
   mysql -h 192.168.1.119 -P 33096 -u ipi -p zcgl < database-init.sql

6. 启动后端服务
   cd backend
   ./start.sh

7. 检查服务状态
   ./status.sh
   tail -f logs/asset-management.log

8. 部署前端（Nginx）
   cd /opt/asset-management
   
   # 方式1：使用自动脚本
   ./nginx-install.sh      # 安装 Nginx（如未安装）
   ./deploy-frontend.sh    # 自动部署前端
   
   # 方式2：手动部署
   # 复制前端文件到 Nginx 目录
   mkdir -p /usr/share/nginx/html/asset-management
   cp -r frontend/dist/* /usr/share/nginx/html/asset-management/
   
   # 配置 Nginx（参考 frontend/nginx.conf）
   cp frontend/nginx.conf /etc/nginx/conf.d/asset-management.conf
   vi /etc/nginx/conf.d/asset-management.conf  # 修改 server_name
   
   # 重启 Nginx
   nginx -t
   systemctl restart nginx

9. 访问系统
   http://your-server-ip
   默认账号: admin
   默认密码: admin123

三、重要配置项
--------------

1. 数据库配置（backend/config/application.yml）
   spring.datasource.url: jdbc:mysql://192.168.1.119:33096/zcgl?...
   spring.datasource.username: ipi
   spring.datasource.password: IPI_i314

2. JWT 密钥（必须修改！）
   jwt.secret: 至少32位的随机字符串

3. 服务端口
   server.port: 8080

4. API 文档（生产环境建议关闭）
   knife4j.enable: false

四、服务管理命令
--------------
cd /opt/asset-management/backend
./start.sh      - 启动服务
./stop.sh       - 停止服务
./restart.sh    - 重启服务
./status.sh     - 查看状态

五、日志文件
--------------
backend/logs/asset-management.log       - 应用日志
backend/logs/asset-management-error.log - 错误日志
backend/logs/console.log                - 控制台输出
/var/log/nginx/asset-management-access.log  - Nginx 访问日志
/var/log/nginx/asset-management-error.log   - Nginx 错误日志

六、故障排查
--------------
1. 查看后端日志
   tail -f backend/logs/asset-management.log

2. 检查端口占用
   netstat -tlnp | grep 8080

3. 检查进程
   ps aux | grep asset-management

4. 测试 API
   curl http://localhost:8080/api/auth/captcha

5. 检查 Nginx
   systemctl status nginx
   tail -f /var/log/nginx/error.log

七、安全建议
--------------
1. 修改默认管理员密码
2. 修改 JWT 密钥
3. 配置防火墙规则
4. 使用 HTTPS（配置 SSL 证书）
5. 定期备份数据库
6. 限制配置文件访问权限: chmod 600 config/application.yml

八、性能优化
--------------
1. JVM 参数调优（在 start.sh 中修改）
   -Xms512m -Xmx1024m
   
2. 数据库连接池配置
3. Nginx 缓存配置
4. 启用 Gzip 压缩

九、备份策略
--------------
1. 数据库定期备份
   mysqldump -h 192.168.1.119 -P 33096 -u ipi -p zcgl > backup.sql

2. 配置文件备份
   tar -czf config-backup.tar.gz config/

3. 日志归档
   定期清理或归档旧日志文件

十、联系支持
--------------
如有问题，请查看详细文档：
- README.md - 完整部署文档
- QUICK_DEPLOY.md - 快速部署指南
- EXTERNAL_CONFIG_GUIDE.md - 配置详解

========================================
构建时间: $(date '+%Y-%m-%d %H:%M:%S')
版本: 1.0.0
========================================
EOF

echo -e "${GREEN}✓ 部署说明已生成${NC}"

# 5. 打包
echo ""
echo "=========================================="
echo "5. 打包部署文件"
echo "=========================================="

PACKAGE_NAME="asset-management-production-$(date +%Y%m%d-%H%M%S).tar.gz"
tar -czf ${PACKAGE_NAME} ${DEPLOY_DIR}
echo -e "${GREEN}✓ 部署包已打包: ${PACKAGE_NAME}${NC}"

# 显示部署包信息
PACKAGE_SIZE=$(du -h ${PACKAGE_NAME} | cut -f1)
echo ""
echo "=========================================="
echo "构建完成！"
echo "=========================================="
echo -e "${GREEN}部署包: ${PACKAGE_NAME}${NC}"
echo -e "${GREEN}大小: ${PACKAGE_SIZE}${NC}"
echo ""
echo "部署包内容:"
echo "  backend/"
echo "    ├── asset-management-1.0.0.jar"
echo "    ├── config/"
echo "    ├── logs/"
echo "    └── *.sh (启动脚本)"
echo "  frontend/"
echo "    ├── dist/"
echo "    └── nginx.conf"
echo "  ├── nginx-install.sh"
echo "  ├── deploy-frontend.sh"
echo "  ├── database-init.sql"
echo "  └── *.md (文档)"
echo ""
echo "下一步操作:"
echo "1. 上传部署包到服务器:"
echo "   scp ${PACKAGE_NAME} root@your-server:/tmp/"
echo ""
echo "2. 在服务器上解压:"
echo "   cd /opt"
echo "   tar -xzf /tmp/${PACKAGE_NAME}"
echo "   cd ${DEPLOY_DIR}"
echo ""
echo "3. 修改配置:"
echo "   vi backend/config/application.yml"
echo ""
echo "4. 启动后端:"
echo "   cd backend"
echo "   chmod +x *.sh"
echo "   ./start.sh"
echo ""
echo "5. 部署前端:"
echo "   cd /opt/${DEPLOY_DIR}"
echo "   chmod +x *.sh"
echo "   ./nginx-install.sh"
echo "   ./deploy-frontend.sh"
echo "=========================================="
