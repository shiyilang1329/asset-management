-- 插入默认管理员用户 (密码: admin123)
INSERT INTO sys_user (username, password, real_name, email, phone, status) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 'admin@company.com', '13800138000', 1);

-- 插入默认资产分类
INSERT INTO asset_category (category_code, category_name, parent_id, level, sort_order) VALUES
('IT', 'IT设备', 0, 1, 1),
('IT-001', '电脑', 1, 2, 1),
('IT-002', '打印机', 1, 2, 2),
('IT-003', '网络设备', 1, 2, 3),
('OFFICE', '办公设备', 0, 1, 2),
('OFFICE-001', '办公桌椅', 5, 2, 1),
('OFFICE-002', '文件柜', 5, 2, 2),
('VEHICLE', '车辆', 0, 1, 3);

-- 插入示例资产数据
INSERT INTO asset (asset_no, asset_name, category_id, brand, model, purchase_price, purchase_date, supplier, status, location) VALUES
('PC-2024-001', '联想ThinkPad笔记本', 2, '联想', 'ThinkPad X1 Carbon', 8999.00, '2024-01-15', '联想官方旗舰店', 1, '研发部-101室'),
('PC-2024-002', '戴尔台式机', 2, '戴尔', 'OptiPlex 7090', 5999.00, '2024-01-20', '戴尔授权经销商', 1, '财务部-201室'),
('PRINTER-2024-001', '惠普激光打印机', 3, '惠普', 'LaserJet Pro M404dn', 2199.00, '2024-02-01', '京东自营', 1, '行政部-301室'),
('ROUTER-2024-001', '华为路由器', 4, '华为', 'AR2220', 3500.00, '2024-01-10', '华为企业网', 1, '机房'),
('DESK-2024-001', '办公桌', 6, '震旦', 'SD-1200', 1200.00, '2024-01-05', '震旦办公家具', 1, '市场部-401室');

COMMIT;
