-- ============================================================
-- 乡镇粮食代收点进销存系统 - 数据库初始化脚本
-- 数据库: grain_purchase_inventory_system
-- 创建时间: 2026-04-10
-- ============================================================

CREATE DATABASE IF NOT EXISTS grain_purchase_inventory_system
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE grain_purchase_inventory_system;

-- ============================================================
-- 一、用户权限相关表
-- ============================================================

-- 用户表
CREATE TABLE IF NOT EXISTS t_user (
    id               INT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    username         VARCHAR(32)  NOT NULL COMMENT '登录账号',
    password         VARCHAR(128) NOT NULL COMMENT 'Bcrypt加密密码',
    real_name        VARCHAR(32)  NULL COMMENT '真实姓名',
    phone            VARCHAR(20)  NULL COMMENT '手机号',
    email            VARCHAR(100) NULL COMMENT '电子邮箱',
    status           TINYINT      NOT NULL DEFAULT 1 COMMENT '状态(0禁用/1正常/2锁定)',
    must_change_pwd  TINYINT      NOT NULL DEFAULT 0 COMMENT '首次登录是否强制改密(0否/1是)',
    login_fail_count TINYINT      NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
    lock_time        DATETIME     NULL COMMENT '锁定到期时间',
    last_login_time  DATETIME     NULL COMMENT '最后登录时间',
    last_login_ip    VARCHAR(64)  NULL COMMENT '最后登录IP',
    is_deleted       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除(0正常/1已删除)',
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_phone (phone),
    KEY idx_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS t_role (
    id          INT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    role_name   VARCHAR(32)  NOT NULL COMMENT '角色名称',
    role_code   VARCHAR(32)  NOT NULL COMMENT '角色编码(ADMIN/OPERATOR/FARMER/DOWNSTREAM)',
    description VARCHAR(200) NULL COMMENT '角色描述',
    is_system   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否内置角色(0否/1是,内置不可删)',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态(0禁用/1启用)',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_name (role_name),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS t_permission (
    id              INT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    parent_id       INT          NOT NULL DEFAULT 0 COMMENT '父权限ID(0为顶层)',
    permission_name VARCHAR(64)  NOT NULL COMMENT '权限显示名称',
    permission_code VARCHAR(128) NULL COMMENT '权限唯一编码(按钮权限必填)',
    permission_type TINYINT      NOT NULL COMMENT '类型(0目录/1菜单/2按钮)',
    route_path      VARCHAR(200) NULL COMMENT '前端路由路径',
    component       VARCHAR(200) NULL COMMENT '前端组件路径',
    icon            VARCHAR(64)  NULL COMMENT '图标',
    sort_order      INT          NOT NULL DEFAULT 0 COMMENT '排序',
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态(0禁用/1启用)',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_permission_code (permission_code),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS t_user_role (
    id             INT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id        INT      NOT NULL COMMENT '用户ID',
    role_id        INT      NOT NULL COMMENT '角色ID',
    create_time    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关联时间',
    create_user_id INT      NULL COMMENT '分配操作人',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_user_id (user_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS t_role_permission (
    id            INT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    role_id       INT      NOT NULL COMMENT '角色ID',
    permission_id INT      NOT NULL COMMENT '权限ID',
    create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_perm (role_id, permission_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 农户档案表
CREATE TABLE IF NOT EXISTS t_farmer (
    id                    INT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id               INT          NOT NULL COMMENT '关联用户ID',
    id_card               VARCHAR(256) NULL COMMENT '身份证号(AES-256加密)',
    id_card_mask          VARCHAR(32)  NULL COMMENT '身份证脱敏展示',
    id_card_hash          VARCHAR(64)  NULL COMMENT '身份证SHA-256哈希(唯一索引用)',
    bank_card             VARCHAR(256) NULL COMMENT '银行卡号(AES-256加密)',
    bank_card_mask        VARCHAR(32)  NULL COMMENT '银行卡脱敏展示',
    bank_name             VARCHAR(64)  NULL COMMENT '开户行',
    preferred_grain_types VARCHAR(256) NULL COMMENT '常售粮食种类(JSON数组)',
    remark                VARCHAR(500) NULL COMMENT '备注',
    audit_status          TINYINT      NOT NULL DEFAULT 1 COMMENT '档案状态(0待审核/1正常/2冻结)',
    create_user_id        INT          NULL COMMENT '创建人',
    create_time           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id),
    UNIQUE KEY uk_id_card_hash (id_card_hash),
    KEY idx_audit_status (audit_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='农户档案表';

-- 下游机构档案表
CREATE TABLE IF NOT EXISTS t_downstream_org (
    id             INT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id        INT          NOT NULL COMMENT '关联用户ID',
    org_name       VARCHAR(128) NOT NULL COMMENT '机构名称',
    org_type       TINYINT      NULL COMMENT '机构类型(0粮库/1加工厂/2贸易商/3其他)',
    contact_name   VARCHAR(32)  NULL COMMENT '联系人',
    phone          VARCHAR(20)  NULL COMMENT '联系电话',
    address        VARCHAR(256) NULL COMMENT '地址',
    credential_url VARCHAR(512) NULL COMMENT '资质文件MinIO路径(JSON数组)',
    audit_status   TINYINT      NOT NULL DEFAULT 0 COMMENT '审核状态(0待审核/1审核通过/2已驳回)',
    audit_user_id  INT          NULL COMMENT '审核人',
    audit_time     DATETIME     NULL COMMENT '审核时间',
    reject_reason  VARCHAR(200) NULL COMMENT '驳回原因',
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id),
    KEY idx_org_audit_status (audit_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='下游机构档案表';

-- 粮食种类表
CREATE TABLE IF NOT EXISTS t_grain (
    id                   INT           NOT NULL AUTO_INCREMENT COMMENT '主键',
    grain_type           VARCHAR(32)   NOT NULL COMMENT '粮食种类(小麦/水稻等)',
    grain_grade          VARCHAR(32)   NULL COMMENT '等级(一等/二等等)',
    ref_purchase_price   DECIMAL(10,4) NULL COMMENT '参考收购单价(元/kg)',
    ref_sale_price       DECIMAL(10,4) NULL COMMENT '参考销售单价(元/kg)',
    moisture_max         DECIMAL(5,2)  NULL COMMENT '允许最大含水率(%)',
    impurity_max         DECIMAL(5,2)  NULL COMMENT '允许最大杂质率(%)',
    storage_temp_min     DECIMAL(5,1)  NULL COMMENT '适宜仓储温度下限(°C)',
    storage_temp_max     DECIMAL(5,1)  NULL COMMENT '适宜仓储温度上限(°C)',
    storage_humidity_max DECIMAL(5,1)  NULL COMMENT '适宜湿度上限(%)',
    low_stock_threshold  DECIMAL(10,2) NULL DEFAULT 500 COMMENT '库存不足预警阈值(kg)',
    max_storage_days     INT           NULL COMMENT '最长仓储天数(超期预警)',
    status               TINYINT       NOT NULL DEFAULT 1 COMMENT '状态(0禁用/1启用)',
    create_time          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_grain_type (grain_type),
    KEY idx_grain_grade (grain_grade)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='粮食种类表';

-- 储位表
CREATE TABLE IF NOT EXISTS t_storage_position (
    id               INT           NOT NULL AUTO_INCREMENT COMMENT '主键',
    position_code    VARCHAR(32)   NOT NULL COMMENT '储位编码(A01/B02等)',
    position_name    VARCHAR(64)   NULL COMMENT '储位名称',
    capacity         DECIMAL(10,2) NULL COMMENT '设计容量(kg)',
    current_stock    DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '当前库存量(实时更新)',
    status           TINYINT       NOT NULL DEFAULT 1 COMMENT '状态(0不可用/1可用/2维修中)',
    sensor_device_id INT           NULL COMMENT '关联温湿度传感器ID',
    remark           VARCHAR(200)  NULL COMMENT '备注',
    create_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_position_code (position_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='储位表';

-- 系统参数表
CREATE TABLE IF NOT EXISTS t_system_param (
    id             INT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    param_key      VARCHAR(64)  NOT NULL COMMENT '参数键',
    param_value    VARCHAR(256) NOT NULL COMMENT '参数值',
    param_desc     VARCHAR(200) NULL COMMENT '参数说明',
    param_group    VARCHAR(32)  NULL COMMENT '参数分组(purchase/inventory/sales/alert)',
    is_editable    TINYINT      NOT NULL DEFAULT 1 COMMENT '是否允许前端编辑',
    update_user_id INT          NULL COMMENT '最后修改人',
    update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_param_key (param_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统参数表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS t_operation_log (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    module       VARCHAR(32)  NOT NULL COMMENT '模块(system/purchase/inventory/sales/hardware)',
    resource     VARCHAR(64)  NOT NULL COMMENT '资源(user/role/farmer/org/grain等)',
    operation    VARCHAR(32)  NOT NULL COMMENT '操作(create/update/delete/audit/status_change)',
    target_id    INT          NULL COMMENT '操作目标ID',
    before_data  JSON         NULL COMMENT '操作前数据',
    after_data   JSON         NULL COMMENT '操作后数据',
    operator_id  INT          NOT NULL COMMENT '操作人ID',
    operate_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    ip_addr      VARCHAR(64)  NULL COMMENT 'IP地址',
    PRIMARY KEY (id),
    KEY idx_log_module_resource (module, resource),
    KEY idx_log_operator (operator_id),
    KEY idx_log_time (operate_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表(只允许INSERT和SELECT)';

-- ============================================================
-- 二、收购管理相关表
-- ============================================================

-- 收购预约表
CREATE TABLE IF NOT EXISTS t_purchase_reserve (
    id             INT           NOT NULL AUTO_INCREMENT COMMENT '主键',
    reserve_no     VARCHAR(32)   NOT NULL COMMENT '预约单号(RV+yyyyMMdd+4位序号)',
    farmer_id      INT           NOT NULL COMMENT '关联农户ID',
    grain_id       INT           NOT NULL COMMENT '粮食品种ID',
    estimated_weight DECIMAL(10,2) NOT NULL COMMENT '预估重量(kg)',
    reserve_time   DATETIME      NOT NULL COMMENT '预约收购时间',
    status         TINYINT       NOT NULL DEFAULT 0 COMMENT '状态(0待审核/1已审核/2已排期/3已取消)',
    reject_reason  VARCHAR(200)  NULL COMMENT '驳回原因',
    audit_user_id  INT           NULL COMMENT '审核人',
    audit_time     DATETIME      NULL COMMENT '审核时间',
    create_user_id INT           NOT NULL COMMENT '创建人',
    create_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_reserve_no (reserve_no),
    KEY idx_reserve_farmer_id (farmer_id),
    KEY idx_reserve_status (status),
    KEY idx_reserve_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收购预约表';

-- 称重记录表
CREATE TABLE IF NOT EXISTS t_weighing_record (
    id              INT           NOT NULL AUTO_INCREMENT COMMENT '主键',
    weigh_no        VARCHAR(32)   NOT NULL COMMENT '称重单号',
    reserve_id      INT           NULL COMMENT '关联预约单(可空)',
    weighbridge_id  INT           NULL COMMENT '地磅设备ID',
    gross_weight    DECIMAL(10,2) NOT NULL COMMENT '毛重(kg)',
    tare_weight     DECIMAL(10,2) NOT NULL COMMENT '皮重(kg)',
    net_weight      DECIMAL(10,2) NOT NULL COMMENT '净重(自动计算)',
    weigh_time      DATETIME      NOT NULL COMMENT '称重时间',
    data_source     TINYINT       NOT NULL DEFAULT 0 COMMENT '数据来源(0自动采集/1手动录入/2断网补录)',
    status          TINYINT       NOT NULL DEFAULT 0 COMMENT '状态(0待审核/1已确认/2已作废)',
    audit_user_id   INT           NULL COMMENT '审核人',
    audit_time      DATETIME      NULL COMMENT '审核时间',
    remark          VARCHAR(200)  NULL COMMENT '备注',
    create_user_id  INT           NOT NULL COMMENT '创建人',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_weigh_no (weigh_no),
    KEY idx_weighbridge_id (weighbridge_id),
    KEY idx_weigh_status (status),
    KEY idx_weigh_time (weigh_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='称重记录表';

-- 收购单据表
CREATE TABLE IF NOT EXISTS t_purchase_order (
    id              INT           NOT NULL AUTO_INCREMENT COMMENT '主键',
    order_no        VARCHAR(32)   NOT NULL COMMENT '收购单号(PO+yyyyMMdd+4位序号)',
    farmer_id       INT           NOT NULL COMMENT '关联农户ID',
    grain_id        INT           NOT NULL COMMENT '粮食品种ID',
    weigh_record_id INT           NOT NULL COMMENT '关联称重记录ID(一对一)',
    actual_weight   DECIMAL(10,2) NOT NULL COMMENT '实际收购重量(kg)',
    ref_price       DECIMAL(10,4) NOT NULL COMMENT '参考单价(元/kg)',
    actual_price    DECIMAL(10,4) NOT NULL COMMENT '实际收购单价(元/kg)',
    total_amount    DECIMAL(12,2) NOT NULL COMMENT '应付总金额',
    paid_amount     DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '已付金额',
    payment_status  TINYINT       NOT NULL DEFAULT 0 COMMENT '付款状态(0未付款/1部分付款/2已付款)',
    status          TINYINT       NOT NULL DEFAULT 0 COMMENT '单据状态(0草稿/1待审核/2审核通过/3已完成/4已作废)',
    void_reason     VARCHAR(200)  NULL COMMENT '作废原因',
    attachment_url  VARCHAR(255)  NULL COMMENT 'MinIO附件路径',
    create_user_id  INT           NOT NULL COMMENT '创建人',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    audit_user_id   INT           NULL COMMENT '审核人',
    audit_time      DATETIME      NULL COMMENT '审核时间',
    update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    UNIQUE KEY uk_weigh_record_id (weigh_record_id),
    KEY idx_po_farmer_id (farmer_id),
    KEY idx_po_grain_id (grain_id),
    KEY idx_po_status (status),
    KEY idx_po_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收购单据表';

-- 付款记录表
CREATE TABLE IF NOT EXISTS t_purchase_payment (
    id          INT           NOT NULL AUTO_INCREMENT COMMENT '主键',
    payment_no  VARCHAR(32)   NOT NULL COMMENT '付款单号',
    order_id    INT           NOT NULL COMMENT '关联收购单据ID',
    farmer_id   INT           NOT NULL COMMENT '关联农户ID',
    pay_amount  DECIMAL(12,2) NOT NULL COMMENT '本次付款金额',
    pay_method  TINYINT       NOT NULL COMMENT '付款方式(0现金/1银行转账/2微信/3支付宝)',
    pay_time    DATETIME      NOT NULL COMMENT '付款时间',
    flow_no     VARCHAR(64)   NULL COMMENT '付款流水号',
    operator_id INT           NOT NULL COMMENT '操作员',
    remark      VARCHAR(200)  NULL COMMENT '备注',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payment_no (payment_no),
    KEY idx_payment_order_id (order_id),
    KEY idx_payment_farmer_id (farmer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='付款记录表';

-- ============================================================
-- 三、仓储管理相关表
-- ============================================================

-- 库存表(核心)
CREATE TABLE IF NOT EXISTS t_inventory (
    id                    INT           NOT NULL AUTO_INCREMENT COMMENT '主键',
    grain_id              INT           NOT NULL COMMENT '粮食品种ID',
    position_id           INT           NOT NULL COMMENT '储位ID',
    current_stock         DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '当前库存量(kg,实时维护)',
    book_stock            DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '账面库存(盘点调账时区分)',
    reserved_stock        DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '预扣量(销售订单审核时锁定)',
    earliest_inbound_time DATETIME      NULL COMMENT '最早入库时间(超期预警用)',
    quality_status        TINYINT       NOT NULL DEFAULT 1 COMMENT '质量状态(0待检/1合格/2降级/3报废)',
    status                TINYINT       NOT NULL DEFAULT 0 COMMENT '库存状态(0待入库/1在库/2盘点锁定/3差异待处理)',
    last_check_time       DATETIME      NULL COMMENT '最后盘点时间',
    update_time           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_time           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_grain_position (grain_id, position_id),
    KEY idx_inventory_grain_id (grain_id),
    KEY idx_inventory_position_id (position_id),
    KEY idx_inventory_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';

-- 库存台账明细表(流水,只允许INSERT)
CREATE TABLE IF NOT EXISTS t_inventory_ledger (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    inventory_id  INT           NOT NULL COMMENT '关联库存ID',
    ledger_type   TINYINT       NOT NULL COMMENT '变动类型(0入库/1出库/2盘点调增/3盘点调减/4质量降级)',
    change_qty    DECIMAL(12,2) NOT NULL COMMENT '本次变动量(正增负减)',
    before_qty    DECIMAL(12,2) NOT NULL COMMENT '变动前库存',
    after_qty     DECIMAL(12,2) NOT NULL COMMENT '变动后库存',
    ref_bill_type TINYINT       NULL COMMENT '关联单据类型(0入库单/1出库单/2盘点单)',
    ref_bill_id   INT           NULL COMMENT '关联单据ID',
    remark        VARCHAR(200)  NULL COMMENT '备注',
    operator_id   INT           NOT NULL COMMENT '操作人',
    operate_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id),
    KEY idx_ledger_inventory_time (inventory_id, operate_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存台账明细表(只允许INSERT)';

-- 入库单表
CREATE TABLE IF NOT EXISTS t_inbound_order (
    id                INT           NOT NULL AUTO_INCREMENT COMMENT '主键',
    inbound_no        VARCHAR(32)   NOT NULL COMMENT '入库单号(IN+yyyyMMdd+4位序号)',
    purchase_order_id INT           NULL COMMENT '关联收购单据(调拨时为空)',
    grain_id          INT           NOT NULL COMMENT '粮食品种ID',
    position_id       INT           NOT NULL COMMENT '储位ID',
    net_weight        DECIMAL(10,2) NOT NULL COMMENT '入库净重(kg)',
    inbound_type      TINYINT       NOT NULL DEFAULT 0 COMMENT '入库类型(0收购入库/1调拨入库/2历史补录)',
    inbound_reason    VARCHAR(200)  NULL COMMENT '非收购入库时必填',
    attachment_url    VARCHAR(512)  NULL COMMENT '附件路径',
    status            TINYINT       NOT NULL DEFAULT 0 COMMENT '状态(0待确认/1已确认/2已作废)',
    confirm_user_id   INT           NULL COMMENT '确认人',
    confirm_time      DATETIME      NULL COMMENT '确认时间',
    create_user_id    INT           NOT NULL COMMENT '创建人',
    create_time       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_inbound_no (inbound_no),
    KEY idx_inbound_purchase_order (purchase_order_id),
    KEY idx_inbound_grain_id (grain_id),
    KEY idx_inbound_position_id (position_id),
    KEY idx_inbound_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库单表';

-- 出库单表
CREATE TABLE IF NOT EXISTS t_outbound_order (
    id               INT           NOT NULL AUTO_INCREMENT COMMENT '主键',
    outbound_no      VARCHAR(32)   NOT NULL COMMENT '出库单号(OUT+yyyyMMdd+4位序号)',
    sales_order_id   INT           NULL COMMENT '关联销售订单(销售出库必填)',
    grain_id         INT           NOT NULL COMMENT '粮食品种ID',
    position_id      INT           NOT NULL COMMENT '储位ID',
    plan_weight      DECIMAL(10,2) NOT NULL COMMENT '计划出库重量(kg)',
    actual_weight    DECIMAL(10,2) NULL COMMENT '实际出库重量(确认时填写)',
    deviation_rate   DECIMAL(5,2)  NULL COMMENT '实际偏差率(%,自动计算)',
    outbound_type    TINYINT       NOT NULL DEFAULT 0 COMMENT '出库类型(0销售出库/1调拨出库/2应急出库)',
    outbound_reason  VARCHAR(200)  NULL COMMENT '出库原因',
    attachment_url   VARCHAR(512)  NULL COMMENT '附件路径',
    batch_no         TINYINT       NOT NULL DEFAULT 1 COMMENT '分批出库批次号',
    status           TINYINT       NOT NULL DEFAULT 0 COMMENT '状态(0待确认/1执行中/2已确认/3已作废)',
    confirm_user_id  INT           NULL COMMENT '确认人',
    confirm_time     DATETIME      NULL COMMENT '确认时间',
    create_user_id   INT           NOT NULL COMMENT '创建人',
    create_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_outbound_no (outbound_no),
    KEY idx_outbound_sales_order (sales_order_id),
    KEY idx_outbound_grain_id (grain_id),
    KEY idx_outbound_position_id (position_id),
    KEY idx_outbound_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库单表';

-- 盘点计划表
CREATE TABLE IF NOT EXISTS t_inventory_check (
    id                  INT           NOT NULL AUTO_INCREMENT COMMENT '主键',
    check_no            VARCHAR(32)   NOT NULL COMMENT '盘点计划编号',
    check_type          TINYINT       NOT NULL COMMENT '盘点类型(0全量/1指定品种/2指定储位)',
    scope_grain_ids     VARCHAR(256)  NULL COMMENT '盘点品种范围(JSON)',
    scope_position_ids  VARCHAR(256)  NULL COMMENT '盘点储位范围(JSON)',
    operator_ids        VARCHAR(256)  NULL COMMENT '分配操作员(JSON数组)',
    deadline            DATETIME      NULL COMMENT '盘点截止时间',
    status              TINYINT       NOT NULL DEFAULT 0 COMMENT '状态(0草稿/1进行中/2待审核/3差异处理中/4已完成)',
    total_items         INT           NULL COMMENT '明细行总数',
    checked_items       INT           NOT NULL DEFAULT 0 COMMENT '已实盘行数',
    match_rate          DECIMAL(5,2)  NULL COMMENT '账实相符率(%)',
    report_url          VARCHAR(512)  NULL COMMENT '盘点报告MinIO路径',
    create_user_id      INT           NOT NULL COMMENT '创建人',
    create_time         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    complete_time       DATETIME      NULL COMMENT '完成时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_check_no (check_no),
    KEY idx_check_status (status),
    KEY idx_check_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘点计划表';

-- 盘点明细表
CREATE TABLE IF NOT EXISTS t_inventory_check_detail (
    id               INT           NOT NULL AUTO_INCREMENT COMMENT '主键',
    check_id         INT           NOT NULL COMMENT '关联盘点计划ID',
    inventory_id     INT           NOT NULL COMMENT '关联库存ID',
    grain_id         INT           NOT NULL COMMENT '粮食品种ID',
    position_id      INT           NOT NULL COMMENT '储位ID',
    book_qty         DECIMAL(12,2) NOT NULL COMMENT '账面数量(计划发布时快照)',
    actual_qty       DECIMAL(12,2) NULL COMMENT '实盘数量(操作员填写)',
    diff_qty         DECIMAL(12,2) NULL COMMENT '差异量(actual_qty-book_qty)',
    diff_rate        DECIMAL(6,2)  NULL COMMENT '差异率(%)',
    is_over_threshold TINYINT      NOT NULL DEFAULT 0 COMMENT '是否超损耗阈值',
    photo_urls       VARCHAR(1024) NULL COMMENT '现场照片(JSON数组,MinIO路径)',
    handle_type      TINYINT       NULL COMMENT '差异处理方式(0调账/1重新盘点)',
    handle_reason    VARCHAR(300)  NULL COMMENT '处理原因',
    handle_user_id   INT           NULL COMMENT '处理人',
    handle_time      DATETIME      NULL COMMENT '处理时间',
    operator_id      INT           NULL COMMENT '实盘操作员',
    check_time       DATETIME      NULL COMMENT '实盘时间',
    PRIMARY KEY (id),
    KEY idx_detail_check_id (check_id),
    KEY idx_detail_inventory_id (inventory_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘点明细表';

-- 库存调账记录表(只允许INSERT)
CREATE TABLE IF NOT EXISTS t_inventory_adjustment (
    id              INT           NOT NULL AUTO_INCREMENT COMMENT '主键',
    inventory_id    INT           NOT NULL COMMENT '关联库存ID',
    check_detail_id INT           NULL COMMENT '关联盘点明细(人工调账时为空)',
    adj_qty         DECIMAL(12,2) NOT NULL COMMENT '调账量(正增负减)',
    adj_reason      VARCHAR(300)  NOT NULL COMMENT '调账原因(必填)',
    before_stock    DECIMAL(12,2) NOT NULL COMMENT '调账前库存',
    after_stock     DECIMAL(12,2) NOT NULL COMMENT '调账后库存',
    approve_user_id INT           NOT NULL COMMENT '审批人',
    approve_time    DATETIME      NULL COMMENT '审批时间',
    create_user_id  INT           NOT NULL COMMENT '创建人',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_adj_inventory_id (inventory_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存调账记录表(只允许INSERT)';

-- ============================================================
-- 四、销售管理相关表
-- ============================================================

-- 销售订单表
CREATE TABLE IF NOT EXISTS t_sales_order (
    id                 INT           NOT NULL AUTO_INCREMENT COMMENT '主键',
    order_no           VARCHAR(32)   NOT NULL COMMENT '销售订单号(SO+yyyyMMdd+4位序号)',
    org_id             INT           NOT NULL COMMENT '下游机构ID',
    grain_id           INT           NOT NULL COMMENT '粮食品种ID',
    position_id        INT           NOT NULL COMMENT '出库储位ID',
    plan_weight        DECIMAL(10,2) NOT NULL COMMENT '计划销售重量(kg)',
    actual_out_weight  DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '实际累计出库重量',
    ref_price          DECIMAL(10,4) NULL COMMENT '参考销售单价',
    actual_price       DECIMAL(10,4) NOT NULL COMMENT '实际销售单价',
    total_amount       DECIMAL(12,2) NOT NULL COMMENT '订单总金额',
    actual_amount      DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '实际应收金额(动态计算)',
    received_amount    DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '已收金额',
    reserved_stock     DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '库存预扣量',
    status             TINYINT       NOT NULL DEFAULT 0 COMMENT '订单状态(0草稿/1待审核/2待出库/3部分出库/4已出库/5已完成/6已驳回/7已作废)',
    out_status         TINYINT       NOT NULL DEFAULT 0 COMMENT '出库状态(0未出库/1部分出库/2已出库)',
    payment_status     TINYINT       NOT NULL DEFAULT 0 COMMENT '收款状态(0未收款/1部分收款/2已收款)',
    void_reason        VARCHAR(200)  NULL COMMENT '作废原因',
    attachment_url     VARCHAR(512)  NULL COMMENT '订单附件MinIO路径',
    audit_user_id      INT           NULL COMMENT '审核人',
    audit_time         DATETIME      NULL COMMENT '审核时间',
    create_user_id     INT           NOT NULL COMMENT '创建人',
    create_time        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_so_org_id (org_id),
    KEY idx_so_grain_id (grain_id),
    KEY idx_so_status (status),
    KEY idx_so_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售订单表';

-- 销售收款单表
CREATE TABLE IF NOT EXISTS t_sales_payment (
    id               INT           NOT NULL AUTO_INCREMENT COMMENT '主键',
    payment_no       VARCHAR(32)   NOT NULL COMMENT '收款单号(SP+yyyyMMdd+4位序号)',
    sales_order_id   INT           NOT NULL COMMENT '关联销售订单(一对一)',
    org_id           INT           NOT NULL COMMENT '下游机构ID',
    total_receivable DECIMAL(12,2) NOT NULL COMMENT '应收总金额',
    received_amount  DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '已收金额',
    status           TINYINT       NOT NULL DEFAULT 0 COMMENT '收款状态(0未收款/1部分收款/2已收款/3已锁定)',
    is_locked        TINYINT       NOT NULL DEFAULT 0 COMMENT '对账确认后锁定',
    create_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payment_no (payment_no),
    UNIQUE KEY uk_sales_order_id (sales_order_id),
    KEY idx_sp_org_id (org_id),
    KEY idx_sp_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售收款单表';

-- 收款流水表(只允许INSERT)
CREATE TABLE IF NOT EXISTS t_sales_payment_record (
    id             INT           NOT NULL AUTO_INCREMENT COMMENT '主键',
    payment_id     INT           NOT NULL COMMENT '关联收款单ID',
    sales_order_id INT           NOT NULL COMMENT '关联销售订单ID',
    org_id         INT           NOT NULL COMMENT '下游机构ID',
    receive_amount DECIMAL(12,2) NOT NULL COMMENT '本次收款金额',
    receive_method TINYINT       NOT NULL COMMENT '收款方式(0现金/1银行转账/2承兑汇票/3微信/4支付宝)',
    receive_time   DATETIME      NOT NULL COMMENT '实际收款时间',
    flow_no        VARCHAR(64)   NULL COMMENT '流水号(线上支付必填)',
    bank_info      VARCHAR(128)  NULL COMMENT '银行信息',
    remark         VARCHAR(200)  NULL COMMENT '备注',
    operator_id    INT           NOT NULL COMMENT '操作员',
    create_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_spr_payment_id (payment_id),
    KEY idx_spr_order_id (sales_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收款流水表(只允许INSERT)';

-- 对账单表
CREATE TABLE IF NOT EXISTS t_reconciliation (
    reconcile_no          VARCHAR(32)   NOT NULL COMMENT '对账单号(RC+yyyyMM+机构ID简码)',
    org_id                INT           NOT NULL COMMENT '对账机构ID',
    period_from           DATE          NOT NULL COMMENT '对账周期起',
    period_to             DATE          NOT NULL COMMENT '对账周期止',
    order_count           INT           NULL COMMENT '涉及订单数',
    total_out_weight      DECIMAL(12,2) NULL COMMENT '期间总出库量(kg)',
    total_receivable      DECIMAL(12,2) NULL COMMENT '期间应收总额',
    total_received        DECIMAL(12,2) NULL COMMENT '期间已收总额',
    total_outstanding     DECIMAL(12,2) NULL COMMENT '未收余额',
    status                TINYINT       NOT NULL DEFAULT 0 COMMENT '状态(0已生成/1待确认/2已确认/3有争议/4争议处理中)',
    seller_confirm_user_id INT          NULL COMMENT '代收点确认人',
    seller_confirm_time   DATETIME      NULL COMMENT '代收点确认时间',
    buyer_confirm_info    VARCHAR(128)  NULL COMMENT '机构确认人信息',
    buyer_confirm_time    DATETIME      NULL COMMENT '机构确认时间',
    dispute_reason        VARCHAR(500)  NULL COMMENT '争议原因',
    resolve_result        VARCHAR(500)  NULL COMMENT '处理结果',
    report_url            VARCHAR(512)  NULL COMMENT '对账报告PDF MinIO路径',
    create_user_id        INT           NOT NULL COMMENT '创建人',
    create_time           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (reconcile_no),
    KEY idx_reconcile_org_id (org_id),
    KEY idx_reconcile_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对账单表';

-- 催款记录表
CREATE TABLE IF NOT EXISTS t_sales_collection_record (
    id             INT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    sales_order_id INT          NOT NULL COMMENT '关联销售订单ID',
    org_id         INT          NOT NULL COMMENT '下游机构ID',
    remind_content VARCHAR(500) NOT NULL COMMENT '催款内容',
    remind_time    DATETIME     NOT NULL COMMENT '催款时间',
    operator_id    INT          NOT NULL COMMENT '操作员',
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_scr_order_id (sales_order_id),
    KEY idx_scr_org_id (org_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='催款记录表';

-- ============================================================
-- 五、硬件数据相关表
-- ============================================================

-- 设备基础表
CREATE TABLE IF NOT EXISTS t_device_base (
    id                  INT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    device_code         VARCHAR(32)  NOT NULL COMMENT '设备编号(WB001/SN001等)',
    device_name         VARCHAR(64)  NOT NULL COMMENT '设备名称',
    device_type         TINYINT      NOT NULL COMMENT '设备类型(0地磅/1温湿度传感器/2粮情监测/3监控摄像头)',
    conn_type           TINYINT      NULL COMMENT '连接方式(0串口/1TCP/2MQTT/3RTSP)',
    serial_port         VARCHAR(32)  NULL COMMENT '串口号(如COM3)',
    ip_addr             VARCHAR(64)  NULL COMMENT '设备IP',
    port                INT          NULL COMMENT '端口号',
    position_id         INT          NULL COMMENT '关联储位ID',
    install_location    VARCHAR(128) NULL COMMENT '安装位置描述',
    status              TINYINT      NOT NULL DEFAULT 0 COMMENT '状态(0未启用/1在线/2离线/3故障/4维护中)',
    last_heartbeat_time DATETIME     NULL COMMENT '最后心跳时间',
    last_data_time      DATETIME     NULL COMMENT '最后数据上报时间',
    maintain_cycle_days INT          NOT NULL DEFAULT 0 COMMENT '维护周期(天,0不提醒)',
    last_maintain_time  DATETIME     NULL COMMENT '上次维护时间',
    next_maintain_time  DATETIME     NULL COMMENT '下次维护提醒时间',
    remark              VARCHAR(200) NULL COMMENT '备注',
    create_user_id      INT          NOT NULL COMMENT '创建人',
    create_time         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_device_code (device_code),
    KEY idx_device_type (device_type),
    KEY idx_device_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备基础表';

-- 环境监测数据表(数据量大,按月分表)
CREATE TABLE IF NOT EXISTS t_env_record (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    device_id   INT           NOT NULL COMMENT '关联设备ID',
    position_id INT           NOT NULL COMMENT '关联储位ID',
    temperature DECIMAL(5,1)  NULL COMMENT '温度(℃)',
    humidity    DECIMAL(5,1)  NULL COMMENT '湿度(%)',
    co2         DECIMAL(8,2)  NULL COMMENT 'CO₂浓度(ppm)',
    grain_temp_1 DECIMAL(5,1) NULL COMMENT '粮堆测温点1(℃)',
    grain_temp_2 DECIMAL(5,1) NULL COMMENT '粮堆测温点2',
    grain_temp_3 DECIMAL(5,1) NULL COMMENT '粮堆测温点3',
    record_time DATETIME      NOT NULL COMMENT '数据记录时间',
    data_source TINYINT       NOT NULL DEFAULT 0 COMMENT '0自动采集/1手动补录',
    is_alert    TINYINT       NOT NULL DEFAULT 0 COMMENT '是否触发预警',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_env_device_time (device_id, record_time),
    KEY idx_env_position_time (position_id, record_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='环境监测数据表';

-- 设备告警记录表
CREATE TABLE IF NOT EXISTS t_device_alert (
    id              INT           NOT NULL AUTO_INCREMENT COMMENT '主键',
    device_id       INT           NOT NULL COMMENT '关联设备ID',
    alert_type      TINYINT       NOT NULL COMMENT '告警类型(0离线/1故障/2温度超限/3湿度超限/4粮情异常/5数据异常)',
    alert_level     TINYINT       NOT NULL DEFAULT 1 COMMENT '告警级别(1普通/2重要/3紧急)',
    alert_content   VARCHAR(512)  NOT NULL COMMENT '告警详细描述',
    alert_time      DATETIME      NOT NULL COMMENT '告警发生时间',
    status          TINYINT       NOT NULL DEFAULT 0 COMMENT '处理状态(0未处理/1已处理/2已忽略/3已恢复)',
    handle_user_id  INT           NULL COMMENT '处理人',
    handle_time     DATETIME      NULL COMMENT '处理时间',
    handle_result   VARCHAR(500)  NULL COMMENT '处理结果',
    sms_sent        TINYINT       NOT NULL DEFAULT 0 COMMENT '短信是否发送成功',
    sms_retry_count TINYINT       NOT NULL DEFAULT 0 COMMENT '短信重试次数',
    position_id     INT           NULL COMMENT '关联储位ID',
    threshold_value DECIMAL(10,2) NULL COMMENT '触发告警阈值',
    actual_value    DECIMAL(10,2) NULL COMMENT '实际检测值',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_alert_device_id (device_id),
    KEY idx_alert_status (status),
    KEY idx_alert_time (alert_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备告警记录表';

-- 告警规则配置表
CREATE TABLE IF NOT EXISTS t_alert_rule (
    id                  INT           NOT NULL AUTO_INCREMENT COMMENT '主键',
    rule_name           VARCHAR(64)   NOT NULL COMMENT '规则名称',
    device_type         TINYINT       NULL COMMENT '适用设备类型(NULL全部)',
    position_id         INT           NULL COMMENT '适用储位(NULL全部)',
    grain_id            INT           NULL COMMENT '适用粮食种类(NULL全部)',
    alert_type          TINYINT       NOT NULL COMMENT '对应告警类型',
    metric_field        VARCHAR(32)   NULL COMMENT '监测指标字段(temperature/humidity)',
    compare_op          TINYINT       NULL COMMENT '比较运算符(0大于/1小于/2大于等于/3小于等于)',
    threshold_value     DECIMAL(10,2) NOT NULL COMMENT '阈值',
    alert_level         TINYINT       NOT NULL DEFAULT 1 COMMENT '告警级别',
    continuous_minutes  INT           NOT NULL DEFAULT 0 COMMENT '持续超限分钟数(0即时触发)',
    recheck_minutes     INT           NOT NULL DEFAULT 10 COMMENT '复检间隔(分钟)',
    enable_sms          TINYINT       NOT NULL DEFAULT 0 COMMENT '是否启用短信通知',
    sms_recipients      VARCHAR(256)  NULL COMMENT '短信接收人(JSON数组)',
    status              TINYINT       NOT NULL DEFAULT 1 COMMENT '状态(0禁用/1启用)',
    create_user_id      INT           NOT NULL COMMENT '创建人',
    create_time         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警规则配置表';

-- 数据同步日志表
CREATE TABLE IF NOT EXISTS t_hardware_sync_log (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    device_id     INT          NOT NULL COMMENT '关联设备ID',
    sync_time     DATETIME     NOT NULL COMMENT '同步时间',
    sync_type     TINYINT      NOT NULL COMMENT '同步类型(0实时采集/1断网补录/2手动导入)',
    data_count    INT          NOT NULL DEFAULT 0 COMMENT '同步数据总条数',
    success_count INT          NOT NULL DEFAULT 0 COMMENT '成功数',
    fail_count    INT          NOT NULL DEFAULT 0 COMMENT '失败数',
    fail_reason   VARCHAR(500) NULL COMMENT '失败原因(JSON)',
    operator_id   INT          NULL COMMENT '手动操作人',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_sync_device_id (device_id),
    KEY idx_sync_time (sync_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据同步日志表';

-- 设备维护记录表
CREATE TABLE IF NOT EXISTS t_device_maintain_record (
    id               INT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    device_id        INT          NOT NULL COMMENT '关联设备ID',
    maintain_type    TINYINT      NOT NULL COMMENT '维护类型(0定期保养/1故障维修/2校准检定/3其他)',
    start_time       DATETIME     NOT NULL COMMENT '维护开始时间',
    end_time         DATETIME     NULL COMMENT '维护结束时间',
    duration_minutes INT          NULL COMMENT '维护时长(分钟)',
    maintain_content VARCHAR(500) NULL COMMENT '维护内容描述',
    maintain_user_id INT          NOT NULL COMMENT '维护操作人',
    result           TINYINT      NULL COMMENT '维护结果(0正常恢复/1更换配件/2需返厂维修)',
    attachment_url   VARCHAR(512) NULL COMMENT '维护记录附件(MinIO)',
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_maintain_device_id (device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备维护记录表';

-- ============================================================
-- 六、统计分析相关表
-- ============================================================

-- 统计快照表
CREATE TABLE IF NOT EXISTS t_stats_snapshot (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    report_type      VARCHAR(32)  NOT NULL COMMENT '报表类型',
    period_type      TINYINT      NOT NULL COMMENT '统计周期(0日/1周/2月/3季/4年)',
    period_value     VARCHAR(16)  NOT NULL COMMENT '统计周期值(如2026-03)',
    dimension_key    VARCHAR(64)  NOT NULL DEFAULT 'all' COMMENT '维度键',
    metric_data      JSON         NOT NULL COMMENT '指标数据',
    is_valid         TINYINT      NOT NULL DEFAULT 1 COMMENT '是否有效(0过期需重算)',
    calc_time        DATETIME     NOT NULL COMMENT '计算时间',
    calc_duration_ms INT          NULL COMMENT '计算耗时(毫秒)',
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_snapshot (report_type, period_type, period_value, dimension_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统计快照表';

-- 异步报表任务表
CREATE TABLE IF NOT EXISTS t_report_task (
    id              INT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    task_no         VARCHAR(32)  NOT NULL COMMENT '任务编号',
    report_type     VARCHAR(32)  NOT NULL COMMENT '报表类型',
    query_params    JSON         NOT NULL COMMENT '查询参数',
    status          TINYINT      NOT NULL DEFAULT 0 COMMENT '任务状态(0排队中/1生成中/2已完成/3失败)',
    progress        TINYINT      NOT NULL DEFAULT 0 COMMENT '进度百分比(0-100)',
    total_rows      INT          NULL COMMENT '预估总行数',
    generated_rows  INT          NOT NULL DEFAULT 0 COMMENT '已生成行数',
    file_url        VARCHAR(512) NULL COMMENT '生成文件的MinIO预签名URL',
    file_size_kb    INT          NULL COMMENT '文件大小(KB)',
    error_msg       VARCHAR(500) NULL COMMENT '失败原因',
    expire_time     DATETIME     NULL COMMENT '下载链接过期时间',
    request_user_id INT          NOT NULL COMMENT '发起人',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    complete_time   DATETIME     NULL COMMENT '完成时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_task_no (task_no),
    KEY idx_task_user_id (request_user_id),
    KEY idx_task_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='异步报表任务表';
