-- ============================================
-- 资产管理系统 - 完整数据库初始化脚本
-- 版本: v1.3.0
-- 日期: 2026-01-15
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS zcgl DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE zcgl;

-- ============================================
-- 1. 系统管理表
-- ============================================

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    real_name VARCHAR(50) COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    department_id BIGINT COMMENT '部门ID',
    status TINYINT DEFAULT 1 COMMENT '状态 1:启用 0:禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_code VARCHAR(50) UNIQUE NOT NULL COMMENT '角色编码',
    role_name VARCHAR(100) NOT NULL COMMENT '角色名称',
    description VARCHAR(500) COMMENT '角色描述',
    status TINYINT DEFAULT 1 COMMENT '状态 1:启用 0:禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    permission_code VARCHAR(100) UNIQUE NOT NULL COMMENT '权限编码',
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    permission_type TINYINT COMMENT '权限类型 1:菜单 2:按钮',
    parent_id BIGINT DEFAULT 0 COMMENT '父权限ID',
    path VARCHAR(200) COMMENT '路由路径',
    icon VARCHAR(50) COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态 1:启用 0:禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_permission_code (permission_code),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id),
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id),
    UNIQUE KEY uk_role_permission (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- ============================================
-- 2. 资产管理表
-- ============================================

-- 资产分类表
CREATE TABLE IF NOT EXISTS asset_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    category_code VARCHAR(50) UNIQUE NOT NULL COMMENT '分类编码',
    category_name VARCHAR(100) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    level INT DEFAULT 1 COMMENT '层级',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_category_code (category_code),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产分类表';

-- 资产表
CREATE TABLE IF NOT EXISTS asset (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '资产ID',
    asset_no VARCHAR(50) UNIQUE NOT NULL COMMENT '资产编号',
    asset_name VARCHAR(200) NOT NULL COMMENT '资产名称',
    category_id BIGINT COMMENT '分类ID',
    brand VARCHAR(100) COMMENT '品牌',
    model VARCHAR(100) COMMENT '型号',
    purchase_price DECIMAL(10,2) COMMENT '采购价格',
    purchase_date DATE COMMENT '采购日期',
    supplier VARCHAR(200) COMMENT '供应商',
    status TINYINT DEFAULT 1 COMMENT '状态 1:在库 2:领用中 3:预约中 4:维修中 5:已报废',
    current_user_id BIGINT COMMENT '当前使用人ID',
    current_department_id BIGINT COMMENT '当前使用部门ID',
    location VARCHAR(500) COMMENT '存放位置',
    remark TEXT COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_asset_no (asset_no),
    INDEX idx_category_id (category_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产表';

-- 资产领用记录表
CREATE TABLE IF NOT EXISTS asset_borrow (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '领用ID',
    asset_id BIGINT NOT NULL COMMENT '资产ID',
    borrower_id BIGINT NOT NULL COMMENT '借用人ID',
    borrow_date DATE NOT NULL COMMENT '借用日期',
    expect_return_date DATE COMMENT '预计归还日期',
    actual_return_date DATE COMMENT '实际归还日期',
    borrow_reason TEXT COMMENT '借用原因',
    status TINYINT DEFAULT 0 COMMENT '状态 0:待生效 1:借出中 2:已归还',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_asset_id (asset_id),
    INDEX idx_borrower_id (borrower_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产领用记录表';

-- 维修记录表
CREATE TABLE IF NOT EXISTS asset_maintenance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '维修ID',
    asset_id BIGINT NOT NULL COMMENT '资产ID',
    report_user_id BIGINT COMMENT '报修人ID',
    report_date DATE COMMENT '报修日期',
    problem_desc TEXT COMMENT '问题描述',
    maintenance_date DATE COMMENT '维修日期',
    maintenance_cost DECIMAL(10,2) COMMENT '维修费用',
    maintenance_result TEXT COMMENT '维修结果',
    status TINYINT DEFAULT 1 COMMENT '状态 1:待维修 2:维修中 3:已完成',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_asset_id (asset_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维修记录表';

-- 报废记录表
CREATE TABLE IF NOT EXISTS asset_scrap (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报废ID',
    asset_id BIGINT NOT NULL COMMENT '资产ID',
    scrap_date DATE COMMENT '报废日期',
    scrap_reason TEXT COMMENT '报废原因',
    operator_id BIGINT COMMENT '操作人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_asset_id (asset_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报废记录表';

-- ============================================
-- 3. 初始化数据
-- ============================================

-- 插入默认管理员用户
-- 用户名: admin, 密码: admin123
INSERT INTO sys_user (username, password, real_name, email, phone, status) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHCIMkdCDmZsO7.IM6UzVG', '系统管理员', 'admin@example.com', '13800138000', 1)
ON DUPLICATE KEY UPDATE username=username;

-- 插入默认角色
INSERT INTO sys_role (role_code, role_name, description, status) VALUES
('ADMIN', '系统管理员', '拥有系统所有权限，可以管理用户、角色和权限', 1),
('MANAGER', '管理者', '拥有业务管理权限，可以管理资产、领用、维修、报废等业务', 1),
('USER', '普通用户', '拥有查看和借用权限，可以查看资产信息并申请借用', 1)
ON DUPLICATE KEY UPDATE role_code=role_code;

-- 插入菜单权限
INSERT INTO sys_permission (permission_code, permission_name, permission_type, parent_id, path, icon, sort_order) VALUES
('dashboard', '仪表盘', 1, 0, '/dashboard', 'DataLine', 1),
('asset', '资产管理', 1, 0, '/asset', 'Box', 2),
('category', '资产分类', 1, 0, '/category', 'Menu', 3),
('borrow', '领用管理', 1, 0, '/borrow', 'DocumentCopy', 4),
('maintenance', '维修管理', 1, 0, '/maintenance', 'Tools', 5),
('scrap', '报废管理', 1, 0, '/scrap', 'Delete', 6),
('user', '用户管理', 1, 0, '/user', 'User', 7),
('role', '角色管理', 1, 0, '/role', 'Avatar', 8)
ON DUPLICATE KEY UPDATE permission_code=permission_code;

-- 获取菜单权限ID
SET @dashboard_id = (SELECT id FROM sys_permission WHERE permission_code = 'dashboard');
SET @asset_id = (SELECT id FROM sys_permission WHERE permission_code = 'asset');
SET @category_id = (SELECT id FROM sys_permission WHERE permission_code = 'category');
SET @borrow_id = (SELECT id FROM sys_permission WHERE permission_code = 'borrow');
SET @maintenance_id = (SELECT id FROM sys_permission WHERE permission_code = 'maintenance');
SET @scrap_id = (SELECT id FROM sys_permission WHERE permission_code = 'scrap');
SET @user_id = (SELECT id FROM sys_permission WHERE permission_code = 'user');
SET @role_id = (SELECT id FROM sys_permission WHERE permission_code = 'role');

-- 插入按钮权限
INSERT INTO sys_permission (permission_code, permission_name, permission_type, parent_id, sort_order) VALUES
-- 资产管理按钮
('asset:view', '查看资产', 2, @asset_id, 1),
('asset:add', '新增资产', 2, @asset_id, 2),
('asset:edit', '编辑资产', 2, @asset_id, 3),
('asset:delete', '删除资产', 2, @asset_id, 4),
('asset:import', '导入资产', 2, @asset_id, 5),
('asset:export', '导出资产', 2, @asset_id, 6),
-- 资产分类按钮
('category:view', '查看分类', 2, @category_id, 1),
('category:add', '新增分类', 2, @category_id, 2),
('category:edit', '编辑分类', 2, @category_id, 3),
('category:delete', '删除分类', 2, @category_id, 4),
-- 领用管理按钮
('borrow:view', '查看领用', 2, @borrow_id, 1),
('borrow:add', '新增领用', 2, @borrow_id, 2),
('borrow:edit', '编辑领用', 2, @borrow_id, 3),
('borrow:delete', '删除领用', 2, @borrow_id, 4),
('borrow:return', '归还资产', 2, @borrow_id, 5),
-- 维修管理按钮
('maintenance:view', '查看维修', 2, @maintenance_id, 1),
('maintenance:add', '新增维修', 2, @maintenance_id, 2),
('maintenance:edit', '编辑维修', 2, @maintenance_id, 3),
('maintenance:delete', '删除维修', 2, @maintenance_id, 4),
-- 报废管理按钮
('scrap:view', '查看报废', 2, @scrap_id, 1),
('scrap:add', '新增报废', 2, @scrap_id, 2),
('scrap:delete', '删除报废', 2, @scrap_id, 3),
-- 用户管理按钮
('user:view', '查看用户', 2, @user_id, 1),
('user:add', '新增用户', 2, @user_id, 2),
('user:edit', '编辑用户', 2, @user_id, 3),
('user:delete', '删除用户', 2, @user_id, 4),
('user:import', '导入用户', 2, @user_id, 5),
('user:assign', '分配角色', 2, @user_id, 6),
('user:reset', '重置密码', 2, @user_id, 7),
-- 角色管理按钮
('role:view', '查看角色', 2, @role_id, 1),
('role:add', '新增角色', 2, @role_id, 2),
('role:edit', '编辑角色', 2, @role_id, 3),
('role:delete', '删除角色', 2, @role_id, 4),
('role:assign', '分配权限', 2, @role_id, 5)
ON DUPLICATE KEY UPDATE permission_code=permission_code;

-- 为ADMIN角色分配所有权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id 
FROM sys_role r, sys_permission p 
WHERE r.role_code = 'ADMIN'
ON DUPLICATE KEY UPDATE role_id=role_id;

-- 为MANAGER角色分配管理权限（除了用户、角色管理）
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id 
FROM sys_role r, sys_permission p 
WHERE r.role_code = 'MANAGER' 
AND p.permission_code IN (
    'dashboard',
    'asset', 'asset:view', 'asset:add', 'asset:edit', 'asset:delete', 'asset:import', 'asset:export',
    'category', 'category:view', 'category:add', 'category:edit', 'category:delete',
    'borrow', 'borrow:view', 'borrow:add', 'borrow:edit', 'borrow:return',
    'maintenance', 'maintenance:view', 'maintenance:add', 'maintenance:edit', 'maintenance:delete',
    'scrap', 'scrap:view', 'scrap:add', 'scrap:delete'
)
ON DUPLICATE KEY UPDATE role_id=role_id;

-- 为USER角色分配查看和借用权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id 
FROM sys_role r, sys_permission p 
WHERE r.role_code = 'USER' 
AND p.permission_code IN (
    'dashboard',
    'asset', 'asset:view',
    'borrow', 'borrow:view', 'borrow:add'
)
ON DUPLICATE KEY UPDATE role_id=role_id;

-- 为admin用户分配ADMIN角色
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id 
FROM sys_user u, sys_role r 
WHERE u.username = 'admin' AND r.role_code = 'ADMIN'
ON DUPLICATE KEY UPDATE user_id=user_id;

-- 插入默认资产分类
INSERT INTO asset_category (category_code, category_name, parent_id, level, sort_order) VALUES
('IT', 'IT设备', 0, 1, 1),
('IT-001', '电脑', 1, 2, 1),
('IT-002', '打印机', 1, 2, 2),
('IT-003', '网络设备', 1, 2, 3),
('OFFICE', '办公设备', 0, 1, 2),
('OFFICE-001', '办公桌椅', 5, 2, 1),
('OFFICE-002', '文件柜', 5, 2, 2),
('VEHICLE', '车辆', 0, 1, 3)
ON DUPLICATE KEY UPDATE category_code=category_code;

-- 插入示例资产数据
INSERT INTO asset (asset_no, asset_name, category_id, brand, model, purchase_price, purchase_date, supplier, status, location) VALUES
('PC-2024-001', '联想ThinkPad笔记本', 2, '联想', 'ThinkPad X1 Carbon', 8999.00, '2024-01-15', '联想官方旗舰店', 1, '研发部-101室'),
('PC-2024-002', '戴尔台式机', 2, '戴尔', 'OptiPlex 7090', 5999.00, '2024-01-20', '戴尔授权经销商', 1, '财务部-201室'),
('PRINTER-2024-001', '惠普激光打印机', 3, '惠普', 'LaserJet Pro M404dn', 2199.00, '2024-02-01', '京东自营', 1, '行政部-301室'),
('ROUTER-2024-001', '华为路由器', 4, '华为', 'AR2220', 3500.00, '2024-01-10', '华为企业网', 1, '机房'),
('DESK-2024-001', '办公桌', 6, '震旦', 'SD-1200', 1200.00, '2024-01-05', '震旦办公家具', 1, '市场部-401室')
ON DUPLICATE KEY UPDATE asset_no=asset_no;

-- ============================================
-- 4. 初始化完成提示
-- ============================================

SELECT '============================================' AS '';
SELECT '数据库初始化完成！' AS message;
SELECT '============================================' AS '';
SELECT '默认管理员账号信息：' AS '';
SELECT '  用户名: admin' AS '';
SELECT '  密码: admin123' AS '';
SELECT '============================================' AS '';
SELECT CONCAT('共创建 ', COUNT(*), ' 个权限') AS permission_count FROM sys_permission;
SELECT CONCAT('共创建 ', COUNT(*), ' 个角色') AS role_count FROM sys_role;
SELECT CONCAT('共创建 ', COUNT(*), ' 个资产分类') AS category_count FROM asset_category;
SELECT CONCAT('共创建 ', COUNT(*), ' 个示例资产') AS asset_count FROM asset;
SELECT '============================================' AS '';


-- ============================================
-- 6. 人员管理模块
-- ============================================

-- 部门字典表
CREATE TABLE IF NOT EXISTS sys_department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '部门ID',
    dept_code VARCHAR(50) UNIQUE NOT NULL COMMENT '部门编码',
    dept_name VARCHAR(100) NOT NULL COMMENT '部门名称',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态 1:启用 0:禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_dept_code (dept_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门字典表';

-- 插入示例部门数据
INSERT INTO sys_department (dept_code, dept_name, sort_order, status) VALUES
('DEV', '研发部', 1, 1),
('MARKET', '市场部', 2, 1),
('FINANCE', '财务部', 3, 1),
('ADMIN', '行政部', 4, 1),
('HR', '人力资源部', 5, 1),
('SALES', '销售部', 6, 1)
ON DUPLICATE KEY UPDATE dept_code=dept_code;

-- 员工表（用于资产领用和维修报修）
CREATE TABLE IF NOT EXISTS employee (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '员工ID',
    employee_no VARCHAR(50) UNIQUE COMMENT '工号',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    department VARCHAR(100) COMMENT '部门',
    position VARCHAR(100) COMMENT '职位',
    status TINYINT DEFAULT 1 COMMENT '状态 1:在职 0:离职',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_name (name),
    INDEX idx_phone (phone),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工表';

-- 插入示例员工数据
INSERT INTO employee (employee_no, name, phone, email, department, position, status) VALUES
('E001', '张三', '13800138001', 'zhangsan@company.com', '研发部', '工程师', 1),
('E002', '李四', '13800138002', 'lisi@company.com', '市场部', '经理', 1),
('E003', '王五', '13800138003', 'wangwu@company.com', '财务部', '会计', 1),
('E004', '赵六', '13800138004', 'zhaoliu@company.com', '行政部', '专员', 1),
('E005', '钱七', '13800138005', 'qianqi@company.com', '研发部', '测试', 1)
ON DUPLICATE KEY UPDATE employee_no=employee_no;

-- 添加人员管理菜单权限
INSERT INTO sys_permission (permission_code, permission_name, permission_type, parent_id, path, icon, sort_order)
VALUES ('employee', '人员管理', 1, 0, '/employee', 'UserFilled', 9)
ON DUPLICATE KEY UPDATE permission_code=permission_code;

-- 获取人员管理菜单ID
SET @employee_id = (SELECT id FROM sys_permission WHERE permission_code = 'employee');

-- 添加人员管理按钮权限
INSERT INTO sys_permission (permission_code, permission_name, permission_type, parent_id, sort_order) VALUES
('employee:view', '查看人员', 2, @employee_id, 1),
('employee:add', '新增人员', 2, @employee_id, 2),
('employee:edit', '编辑人员', 2, @employee_id, 3),
('employee:delete', '删除人员', 2, @employee_id, 4),
('employee:import', '导入人员', 2, @employee_id, 5)
ON DUPLICATE KEY UPDATE permission_code=permission_code;

-- 为ADMIN角色分配人员管理权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id 
FROM sys_role r, sys_permission p 
WHERE r.role_code = 'ADMIN' 
AND p.permission_code LIKE 'employee%'
ON DUPLICATE KEY UPDATE role_id=role_id;

-- 为MANAGER角色分配人员管理权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id 
FROM sys_role r, sys_permission p 
WHERE r.role_code = 'MANAGER' 
AND p.permission_code IN ('employee', 'employee:view', 'employee:add', 'employee:edit', 'employee:import')
ON DUPLICATE KEY UPDATE role_id=role_id;

-- ============================================
-- 初始化完成
-- ============================================

SELECT '=== 数据库初始化完成 ===' AS '';
SELECT CONCAT('用户数: ', COUNT(*)) AS user_count FROM sys_user;
SELECT CONCAT('角色数: ', COUNT(*)) AS role_count FROM sys_role;
SELECT CONCAT('权限数: ', COUNT(*)) AS permission_count FROM sys_permission;
SELECT CONCAT('资产分类数: ', COUNT(*)) AS category_count FROM asset_category;
SELECT CONCAT('员工数: ', COUNT(*)) AS employee_count FROM employee;
SELECT '默认管理员账号: admin / admin123' AS '';
SELECT '请重新登录以刷新权限' AS '';
