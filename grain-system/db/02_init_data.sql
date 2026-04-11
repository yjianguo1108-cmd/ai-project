-- ============================================================
-- 初始化基础数据
-- ============================================================
USE grain_purchase_inventory_system;

-- 初始化角色
INSERT INTO t_role (role_name, role_code, description, is_system, status) VALUES
('超级管理员', 'ADMIN', '系统超级管理员，拥有所有权限', 1, 1),
('操作员', 'OPERATOR', '代收点操作员，处理日常业务', 1, 1),
('农户', 'FARMER', '售粮农户，可提交预约', 1, 1),
('下游机构', 'DOWNSTREAM', '下游采购机构', 1, 1);

-- 初始化权限树（目录）
INSERT INTO t_permission (parent_id, permission_name, permission_code, permission_type, route_path, component, icon, sort_order) VALUES
(0, '基础管理', NULL, 0, '/system', NULL, 'Setting', 10),
(0, '收购管理', NULL, 0, '/purchase', NULL, 'ShoppingCart', 20),
(0, '仓储管理', NULL, 0, '/inventory', NULL, 'Box', 30),
(0, '销售管理', NULL, 0, '/sales', NULL, 'TrendCharts', 40),
(0, '硬件对接', NULL, 0, '/hardware', NULL, 'Monitor', 50),
(0, '统计分析', NULL, 0, '/stats', NULL, 'DataAnalysis', 60);

-- 菜单权限（基础管理）
INSERT INTO t_permission (parent_id, permission_name, permission_code, permission_type, route_path, component, sort_order) VALUES
(1, '用户管理', NULL, 1, '/system/user', 'system/UserManage', 11),
(1, '角色管理', NULL, 1, '/system/role', 'system/RoleManage', 12),
(1, '农户档案', NULL, 1, '/system/farmer', 'system/FarmerManage', 13),
(1, '下游机构', NULL, 1, '/system/org', 'system/OrgManage', 14),
(1, '粮食配置', NULL, 1, '/system/grain', 'system/GrainConfig', 15),
(1, '储位管理', NULL, 1, '/system/position', 'system/PositionManage', 16),
(1, '系统参数', NULL, 1, '/system/params', 'system/SysParams', 17);

-- 按钮权限（用户管理）
INSERT INTO t_permission (parent_id, permission_name, permission_code, permission_type, sort_order) VALUES
(7, '新增用户', 'system:user:create', 2, 1),
(7, '编辑用户', 'system:user:update', 2, 2),
(7, '删除用户', 'system:user:delete', 2, 3),
(7, '启用/禁用', 'system:user:status', 2, 4),
(7, '重置密码', 'system:user:reset_pwd', 2, 5),
(8, '新增角色', 'system:role:create', 2, 1),
(8, '编辑角色', 'system:role:update', 2, 2),
(8, '删除角色', 'system:role:delete', 2, 3),
(8, '分配权限', 'system:role:assign_perm', 2, 4),
(9, '新增农户', 'system:farmer:create', 2, 1),
(9, '编辑农户', 'system:farmer:update', 2, 2),
(9, '导入农户', 'system:farmer:import', 2, 3),
(9, '导出农户', 'system:farmer:export', 2, 4),
(9, '查看敏感信息', 'system:farmer:decrypt', 2, 5),
(10, '新增机构', 'system:org:create', 2, 1),
(10, '审核机构', 'system:org:audit', 2, 2);

-- 菜单权限（收购管理）
INSERT INTO t_permission (parent_id, permission_name, permission_code, permission_type, route_path, component, sort_order) VALUES
(2, '收购预约', NULL, 1, '/purchase/reserve', 'purchase/ReserveManage', 21),
(2, '称重记录', NULL, 1, '/purchase/weighing', 'purchase/WeighingRecord', 22),
(2, '收购单据', NULL, 1, '/purchase/order', 'purchase/PurchaseOrder', 23),
(2, '付款管理', NULL, 1, '/purchase/payment', 'purchase/PaymentManage', 24);

-- 菜单权限（仓储管理）
INSERT INTO t_permission (parent_id, permission_name, permission_code, permission_type, route_path, component, sort_order) VALUES
(3, '入库管理', NULL, 1, '/inventory/inbound', 'inventory/InboundOrder', 31),
(3, '库存管理', NULL, 1, '/inventory/stock', 'inventory/StockManage', 32),
(3, '盘点管理', NULL, 1, '/inventory/check', 'inventory/CheckManage', 33),
(3, '出库管理', NULL, 1, '/inventory/outbound', 'inventory/OutboundOrder', 34);

-- 菜单权限（销售管理）
INSERT INTO t_permission (parent_id, permission_name, permission_code, permission_type, route_path, component, sort_order) VALUES
(4, '销售订单', NULL, 1, '/sales/order', 'sales/SalesOrder', 41),
(4, '出库核销', NULL, 1, '/sales/outbound', 'sales/OutboundVerify', 42),
(4, '收款管理', NULL, 1, '/sales/payment', 'sales/PaymentReceive', 43),
(4, '对账管理', NULL, 1, '/sales/reconcile', 'sales/Reconciliation', 44);

-- 菜单权限（硬件对接）
INSERT INTO t_permission (parent_id, permission_name, permission_code, permission_type, route_path, component, sort_order) VALUES
(5, '设备管理', NULL, 1, '/hardware/device', 'hardware/DeviceManage', 51),
(5, '称重数据', NULL, 1, '/hardware/weighing', 'hardware/WeighingData', 52),
(5, '环境监测', NULL, 1, '/hardware/env', 'hardware/EnvMonitor', 53),
(5, '告警管理', NULL, 1, '/hardware/alert', 'hardware/AlertManage', 54),
(5, '视频监控', NULL, 1, '/hardware/camera', 'hardware/CameraMonitor', 55);

-- 菜单权限（统计分析）
INSERT INTO t_permission (parent_id, permission_name, permission_code, permission_type, route_path, component, sort_order) VALUES
(6, '经营仪表盘', NULL, 1, '/stats/dashboard', 'stats/Dashboard', 61),
(6, '收购统计', NULL, 1, '/stats/purchase', 'stats/PurchaseStats', 62),
(6, '仓储统计', NULL, 1, '/stats/inventory', 'stats/InventoryStats', 63),
(6, '销售统计', NULL, 1, '/stats/sales', 'stats/SalesStats', 64);

-- 初始化管理员用户（密码: admin123，Bcrypt加密后的值）
INSERT INTO t_user (username, password, real_name, phone, status, must_change_pwd) VALUES
('admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '系统管理员', '13800000001', 1, 0);

-- 给管理员分配ADMIN角色
INSERT INTO t_user_role (user_id, role_id, create_user_id) VALUES (1, 1, 1);

-- 给ADMIN角色分配所有权限
INSERT INTO t_role_permission (role_id, permission_id)
SELECT 1, id FROM t_permission;

-- 初始化系统参数
INSERT INTO t_system_param (param_key, param_value, param_desc, param_group, is_editable) VALUES
('purchase_price_min_ratio', '0.8', '收购价格最低偏离比例(相对参考价)', 'purchase', 1),
('purchase_price_max_ratio', '1.2', '收购价格最高偏离比例', 'purchase', 1),
('purchase_weight_deviation', '0.2', '收购重量偏差阈值(%)', 'purchase', 1),
('sale_price_min_ratio', '0.85', '销售价格最低偏离比例', 'sales', 1),
('sale_price_max_ratio', '1.3', '销售价格最高偏离比例', 'sales', 1),
('inventory_check_loss_threshold', '0.02', '盘点损耗阈值(%)', 'inventory', 1),
('inventory_outbound_deviation', '0.01', '出库偏差阈值(%)', 'inventory', 1),
('high_stock_days', '90', '库存积压预警天数', 'inventory', 1),
('payment_overdue_days', '30', '应收款逾期预警天数', 'sales', 1),
('payment_tolerance', '1.0', '收款偏差容忍金额(元)', 'sales', 1),
('video_retention_days', '30', '监控视频保留天数', 'alert', 1),
('device_offline_alert_minutes', '5', '设备离线触发告警分钟数', 'alert', 1);

-- 初始化粮食种类
INSERT INTO t_grain (grain_type, grain_grade, ref_purchase_price, ref_sale_price, moisture_max, impurity_max, storage_temp_min, storage_temp_max, storage_humidity_max, low_stock_threshold, max_storage_days, status) VALUES
('小麦', '一等', 1.3200, 1.5000, 12.5, 1.0, 5.0, 30.0, 75.0, 500, 180, 1),
('小麦', '二等', 1.2800, 1.4500, 13.0, 1.5, 5.0, 30.0, 75.0, 500, 180, 1),
('水稻', '一等', 1.4500, 1.6500, 14.5, 1.0, 5.0, 28.0, 80.0, 500, 150, 1),
('水稻', '二等', 1.4000, 1.6000, 15.0, 1.5, 5.0, 28.0, 80.0, 500, 150, 1),
('玉米', '一等', 1.1000, 1.2500, 14.0, 1.0, 5.0, 32.0, 75.0, 500, 200, 1),
('大豆', '一等', 3.2000, 3.6000, 13.0, 1.0, 5.0, 25.0, 70.0, 500, 120, 1);

-- 初始化储位
INSERT INTO t_storage_position (position_code, position_name, capacity, current_stock, status) VALUES
('A01', 'A区01号仓', 50000.00, 0.00, 1),
('A02', 'A区02号仓', 50000.00, 0.00, 1),
('B01', 'B区01号仓', 80000.00, 0.00, 1),
('B02', 'B区02号仓', 80000.00, 0.00, 1),
('C01', 'C区01号仓', 30000.00, 0.00, 1);

-- 初始化内置告警规则
INSERT INTO t_alert_rule (rule_name, device_type, grain_id, alert_type, metric_field, compare_op, threshold_value, alert_level, continuous_minutes, recheck_minutes, enable_sms, status, create_user_id) VALUES
('小麦仓储温度告警', 1, 1, 2, 'temperature', 0, 30.0, 2, 10, 10, 0, 1, 1),
('小麦仓储湿度告警', 1, 1, 3, 'humidity', 0, 75.0, 2, 10, 10, 0, 1, 1),
('水稻仓储温度告警', 1, 3, 2, 'temperature', 0, 28.0, 2, 10, 10, 0, 1, 1),
('水稻仓储湿度告警', 1, 3, 3, 'humidity', 0, 80.0, 2, 10, 10, 0, 1, 1),
('粮堆高温紧急告警', 2, NULL, 4, 'grain_temp_1', 0, 35.0, 3, 0, 5, 1, 1, 1),
('设备离线告警', NULL, NULL, 0, NULL, NULL, NULL, 1, 5, 30, 0, 1, 1);
