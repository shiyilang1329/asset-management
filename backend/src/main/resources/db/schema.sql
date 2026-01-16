-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    department_id BIGINT COMMENT '部门ID',
    status TINYINT DEFAULT 1 COMMENT '状态 1:启用 0:禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 资产分类表
CREATE TABLE IF NOT EXISTS asset_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_code VARCHAR(50) UNIQUE NOT NULL COMMENT '分类编码',
    category_name VARCHAR(100) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    level INT DEFAULT 1 COMMENT '层级',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产分类表';

-- 资产表
CREATE TABLE IF NOT EXISTS asset (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    asset_no VARCHAR(50) UNIQUE NOT NULL COMMENT '资产编号',
    asset_name VARCHAR(200) NOT NULL COMMENT '资产名称',
    category_id BIGINT COMMENT '分类ID',
    brand VARCHAR(100) COMMENT '品牌',
    model VARCHAR(100) COMMENT '型号',
    purchase_price DECIMAL(10,2) COMMENT '采购价格',
    purchase_date DATE COMMENT '采购日期',
    supplier VARCHAR(200) COMMENT '供应商',
    status TINYINT COMMENT '状态 1:在库 2:领用 3:维修 4:调拨 5:报废',
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
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    asset_id BIGINT NOT NULL COMMENT '资产ID',
    borrower_id BIGINT NOT NULL COMMENT '借用人ID',
    borrow_date DATE NOT NULL COMMENT '借用日期',
    expect_return_date DATE COMMENT '预计归还日期',
    actual_return_date DATE COMMENT '实际归还日期',
    borrow_reason TEXT COMMENT '借用原因',
    status TINYINT COMMENT '状态 1:借出中 2:已归还',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_asset_id (asset_id),
    INDEX idx_borrower_id (borrower_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产领用记录表';

-- 维修记录表
CREATE TABLE IF NOT EXISTS asset_maintenance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    asset_id BIGINT NOT NULL COMMENT '资产ID',
    report_user_id BIGINT COMMENT '报修人ID',
    report_date DATE COMMENT '报修日期',
    problem_desc TEXT COMMENT '问题描述',
    maintenance_date DATE COMMENT '维修日期',
    maintenance_cost DECIMAL(10,2) COMMENT '维修费用',
    maintenance_result TEXT COMMENT '维修结果',
    status TINYINT COMMENT '状态 1:待维修 2:维修中 3:已完成',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_asset_id (asset_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维修记录表';

-- 调拨记录表
CREATE TABLE IF NOT EXISTS asset_transfer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    asset_id BIGINT NOT NULL COMMENT '资产ID',
    from_department_id BIGINT COMMENT '调出部门ID',
    to_department_id BIGINT COMMENT '调入部门ID',
    transfer_date DATE COMMENT '调拨日期',
    transfer_reason TEXT COMMENT '调拨原因',
    operator_id BIGINT COMMENT '操作人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_asset_id (asset_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调拨记录表';

-- 报废记录表
CREATE TABLE IF NOT EXISTS asset_scrap (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    asset_id BIGINT NOT NULL COMMENT '资产ID',
    scrap_date DATE COMMENT '报废日期',
    scrap_reason TEXT COMMENT '报废原因',
    operator_id BIGINT COMMENT '操作人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_asset_id (asset_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报废记录表';
