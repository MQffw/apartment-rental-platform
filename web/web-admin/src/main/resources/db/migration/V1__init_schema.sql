-- ============================================================
-- V1: 初始表结构（34 张表）
-- 依据 model 模块实体逆向生成；枚举字段存储数字 code（@EnumValue）
-- ============================================================

-- ---------------- 地理信息 ----------------
CREATE TABLE province_info
(
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted  TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-在册 1-已删除',
    name        VARCHAR(64) NULL COMMENT '省份名称',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '省份信息表';

CREATE TABLE city_info
(
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted  TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-在册 1-已删除',
    name        VARCHAR(64) NULL COMMENT '城市名称',
    province_id INT         NULL COMMENT '所属省份id',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '城市信息表';

CREATE TABLE district_info
(
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted  TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-在册 1-已删除',
    name        VARCHAR(64) NULL COMMENT '区域名称',
    city_id     INT         NULL COMMENT '所属城市id',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '地区信息表';

-- ---------------- 房源主表 ----------------
CREATE TABLE apartment_info
(
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-在册 1-已删除',
    name           VARCHAR(128) NULL COMMENT '公寓名称',
    introduction   TEXT         NULL COMMENT '公寓介绍',
    district_id    BIGINT       NULL COMMENT '所处区域id',
    district_name  VARCHAR(64)  NULL COMMENT '所处区域名称',
    city_id        BIGINT       NULL COMMENT '所处城市id',
    city_name      VARCHAR(64)  NULL COMMENT '所处城市名称',
    province_id    BIGINT       NULL COMMENT '所处省份id',
    province_name  VARCHAR(64)  NULL COMMENT '所处省份名称',
    address_detail VARCHAR(255) NULL COMMENT '详细地址',
    latitude       VARCHAR(32)  NULL COMMENT '经度',
    longitude      VARCHAR(32)  NULL COMMENT '纬度',
    phone          VARCHAR(20)  NULL COMMENT '公寓前台电话',
    is_release     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否发布：1-已发布 0-未发布',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '公寓信息表';

CREATE TABLE room_info
(
    id           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted   TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-在册 1-已删除',
    room_number  VARCHAR(64)   NULL COMMENT '房间号',
    rent         DECIMAL(10,2) NULL COMMENT '租金（元/月）',
    apartment_id BIGINT        NULL COMMENT '所属公寓id',
    is_release   TINYINT       NOT NULL DEFAULT 0 COMMENT '是否发布：1-已发布 0-未发布',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '房间信息表';

CREATE TABLE graph_info
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted  TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-在册 1-已删除',
    name        VARCHAR(128) NULL COMMENT '图片名称',
    item_type   TINYINT      NULL COMMENT '图片所属对象类型：1-公寓 2-房间',
    item_id     BIGINT       NULL COMMENT '图片所属对象id',
    url         VARCHAR(512) NULL COMMENT '图片地址',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '图片信息表';

-- ---------------- 字典表 ----------------
CREATE TABLE attr_key
(
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted  TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-在册 1-已删除',
    name        VARCHAR(64) NULL COMMENT '属性key',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '房间基本属性表';

CREATE TABLE attr_value
(
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted  TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-在册 1-已删除',
    name        VARCHAR(64) NULL COMMENT '属性value',
    attr_key_id BIGINT      NULL COMMENT '对应的属性key_id',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '房间基本属性值表';

CREATE TABLE facility_info
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted  TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-在册 1-已删除',
    type        TINYINT      NULL COMMENT '配套所属对象类型：1-公寓 2-房间',
    name        VARCHAR(64)  NULL COMMENT '名称',
    icon        VARCHAR(255) NULL COMMENT '图标',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '配套信息表';

CREATE TABLE label_info
(
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted  TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-在册 1-已删除',
    type        TINYINT     NULL COMMENT '类型：1-公寓 2-房间',
    name        VARCHAR(64) NULL COMMENT '标签名称',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '标签信息表';

CREATE TABLE fee_key
(
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted  TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-在册 1-已删除',
    name        VARCHAR(64) NULL COMMENT '付款项key',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '杂项费用名称表';

CREATE TABLE fee_value
(
    id          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted  TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-在册 1-已删除',
    name        VARCHAR(64)    NULL COMMENT '费用value',
    unit        VARCHAR(32)    NULL COMMENT '收费单位',
    fee_key_id  BIGINT         NULL COMMENT '费用所对的fee_key编码',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '杂项费用值表';

CREATE TABLE payment_type
(
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-在册 1-已删除',
    name            VARCHAR(64)  NULL COMMENT '付款方式名称',
    pay_month_count VARCHAR(16)  NULL COMMENT '每次支付租期数',
    additional_info VARCHAR(255) NULL COMMENT '付费说明',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '支付方式表';

CREATE TABLE lease_term
(
    id           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted   TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-在册 1-已删除',
    month_count  INT         NULL COMMENT '租期月数',
    unit         VARCHAR(16) NULL COMMENT '租期单位：月',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '租期信息表';

-- ---------------- 房源关联表 ----------------
CREATE TABLE apartment_facility
(
    id           BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted   TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-在册 1-已删除',
    apartment_id BIGINT   NULL COMMENT '公寓id',
    facility_id  BIGINT   NULL COMMENT '设施id',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '公寓&配套关系';

CREATE TABLE apartment_label
(
    id           BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted   TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-在册 1-已删除',
    apartment_id BIGINT   NULL COMMENT '公寓id',
    label_id     BIGINT   NULL COMMENT '标签id',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '公寓标签关联表';

CREATE TABLE apartment_fee_value
(
    id            BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted    TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-在册 1-已删除',
    apartment_id  BIGINT   NULL COMMENT '公寓id',
    fee_value_id  BIGINT   NULL COMMENT '收费项value_id',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '公寓&杂费关联表';

CREATE TABLE room_attr_value
(
    id            BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted    TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-在册 1-已删除',
    room_id       BIGINT   NULL COMMENT '房间id',
    attr_value_id BIGINT   NULL COMMENT '属性值id',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '房间&基本属性值关联表';

CREATE TABLE room_facility
(
    id           BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted   TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-在册 1-已删除',
    room_id      BIGINT   NULL COMMENT '房间id',
    facility_id  BIGINT   NULL COMMENT '房间设施id',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '房间&配套关联表';

CREATE TABLE room_label
(
    id          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted  TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-在册 1-已删除',
    room_id     BIGINT   NULL COMMENT '房间id',
    label_id    BIGINT   NULL COMMENT '标签id',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '房间&标签关联表';

CREATE TABLE room_payment_type
(
    id              BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted      TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-在册 1-已删除',
    room_id         BIGINT   NULL COMMENT '房间id',
    payment_type_id BIGINT   NULL COMMENT '支付类型id',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '房间&支付方式关联表';

CREATE TABLE room_lease_term
(
    id            BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted    TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-在册 1-已删除',
    room_id       BIGINT   NULL COMMENT '房间id',
    lease_term_id BIGINT   NULL COMMENT '租期id',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '房间租期关系表';

-- ---------------- 租赁业务表 ----------------
CREATE TABLE lease_agreement
(
    id                    BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted            TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-在册 1-已删除',
    email                 VARCHAR(64)   NULL COMMENT '承租人邮箱',
    name                  VARCHAR(64)   NULL COMMENT '承租人姓名',
    phone                 VARCHAR(20)   NULL COMMENT '承租人手机号码',
    identification_number VARCHAR(32)   NULL COMMENT '承租人身份证号码',
    apartment_id          BIGINT        NULL COMMENT '签约公寓id',
    room_id               BIGINT        NULL COMMENT '签约房间id',
    lease_start_date      DATE          NULL COMMENT '租约开始日期',
    lease_end_date        DATE          NULL COMMENT '租约结束日期',
    lease_term_id         BIGINT        NULL COMMENT '租期id',
    rent                  DECIMAL(10,2) NULL COMMENT '租金（元/月）',
    deposit               DECIMAL(10,2) NULL COMMENT '押金（元）',
    payment_type_id       BIGINT        NULL COMMENT '支付类型id',
    status                TINYINT       NOT NULL DEFAULT 1 COMMENT '租约状态：1-签约待确认 2-已签约 3-已取消 4-已到期 5-退租待确认 6-已退租 7-续约待确认',
    source_type           TINYINT       NOT NULL DEFAULT 1 COMMENT '租约来源：1-新签 2-续约',
    additional_info       VARCHAR(512)  NULL COMMENT '备注信息',
    previous_agreement_id BIGINT        NULL COMMENT '续签来源租约ID',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '租约信息表';

CREATE TABLE view_appointment
(
    id                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-在册 1-已删除',
    user_id            BIGINT       NULL COMMENT '用户id',
    name               VARCHAR(64)  NULL COMMENT '用户姓名',
    phone              VARCHAR(20)  NULL COMMENT '用户手机号码',
    apartment_id       BIGINT       NULL COMMENT '公寓id',
    appointment_time   DATETIME     NULL COMMENT '预约时间',
    additional_info    VARCHAR(512) NULL COMMENT '备注信息',
    appointment_status TINYINT      NOT NULL DEFAULT 1 COMMENT '预约状态：1-待看房 2-已取消 3-已看房',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '预约看房信息表';

CREATE TABLE payment_record
(
    id             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted     TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-在册 1-已删除',
    agreement_id   BIGINT        NULL COMMENT '关联租约ID',
    user_id        BIGINT        NULL COMMENT '用户ID',
    amount         DECIMAL(10,2) NULL COMMENT '支付金额',
    payment_type   TINYINT       NULL COMMENT '支付类型：1-租金 2-押金',
    payment_status TINYINT       NOT NULL DEFAULT 0 COMMENT '支付状态：0-待支付 1-已支付 2-支付失败',
    bill_type      TINYINT       NULL COMMENT '账单类型：1-房租 2-电费 3-水费 4-物业费',
    payment_time   DATETIME      NULL COMMENT '支付时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '支付记录表';

CREATE TABLE browsing_history
(
    id          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted  TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-在册 1-已删除',
    user_id     BIGINT   NULL COMMENT '用户id',
    room_id     BIGINT   NULL COMMENT '房间id',
    browse_time DATETIME NULL COMMENT '浏览时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '浏览历史表';

CREATE TABLE repair_request
(
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted   TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-在册 1-已删除',
    user_id      BIGINT       NULL COMMENT '用户ID',
    room_id      BIGINT       NULL COMMENT '房间ID',
    apartment_id BIGINT       NULL COMMENT '公寓ID',
    title        VARCHAR(128) NULL COMMENT '报修标题',
    description  VARCHAR(512) NULL COMMENT '问题描述',
    images       TEXT         NULL COMMENT '图片URL,逗号分隔',
    status       TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0-待处理 1-处理中 2-已完成 3-已关闭',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '报修工单表';

-- ---------------- 用户表 ----------------
CREATE TABLE user_info
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted  TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-在册 1-已删除',
    phone       VARCHAR(20)  NULL COMMENT '手机号码',
    password    VARCHAR(128) NULL COMMENT '密码（BCrypt）',
    email       VARCHAR(64)  NULL COMMENT '邮箱（登录账号）',
    avatar_url  VARCHAR(512) NULL COMMENT '头像url',
    nickname    VARCHAR(64)  NULL COMMENT '昵称',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '账号状态：1-正常 0-禁用',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户信息表';

CREATE TABLE system_user
(
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-在册 1-已删除',
    username        VARCHAR(64)  NULL COMMENT '用户名',
    password        VARCHAR(128) NULL COMMENT '密码（BCrypt）',
    name            VARCHAR(64)  NULL COMMENT '姓名',
    type            TINYINT      NOT NULL DEFAULT 1 COMMENT '用户类型：0-管理员 1-普通用户',
    phone           VARCHAR(20)  NULL COMMENT '手机号码',
    avatar_url      VARCHAR(512) NULL COMMENT '头像地址',
    additional_info VARCHAR(512) NULL COMMENT '备注信息',
    post_id         BIGINT       NULL COMMENT '岗位id',
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '账号状态：1-正常 0-禁用',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '员工信息表';

CREATE TABLE system_post
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted  TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-在册 1-已删除',
    code        VARCHAR(64)  NULL COMMENT '岗位编码',
    name        VARCHAR(64)  NULL COMMENT '岗位名称',
    description VARCHAR(255) NULL COMMENT '岗位描述信息',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '岗位状态：1-正常 0-禁用',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '岗位信息表';

-- ---------------- 社区与通知 ----------------
CREATE TABLE post_info
(
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted    TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-在册 1-已删除',
    user_id       BIGINT       NULL COMMENT '发帖人ID',
    apartment_id  BIGINT       NULL COMMENT '关联公寓ID',
    room_id       BIGINT       NULL COMMENT '关联房间ID',
    title         VARCHAR(128) NULL COMMENT '标题',
    content       TEXT         NULL COMMENT '正文',
    images        TEXT         NULL COMMENT '图片URL,JSON数组',
    rating        TINYINT      NULL COMMENT '评分 1-5',
    like_count    INT          NOT NULL DEFAULT 0 COMMENT '点赞数',
    comment_count INT          NOT NULL DEFAULT 0 COMMENT '评论数',
    status        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1-正常 2-审核中 3-已屏蔽',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '帖子/点评表';

CREATE TABLE post_comment
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted  TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-在册 1-已删除',
    post_id     BIGINT       NULL COMMENT '帖子ID',
    user_id     BIGINT       NULL COMMENT '评论人ID',
    parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '父评论ID（0=一级评论）',
    reply_to_id BIGINT       NOT NULL DEFAULT 0 COMMENT '回复目标用户ID',
    content     VARCHAR(1024) NULL COMMENT '评论内容',
    like_count  INT          NOT NULL DEFAULT 0 COMMENT '点赞数',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1-正常 3-已删除',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '帖子评论表';

CREATE TABLE post_like
(
    id          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted  TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-在册 1-已删除',
    post_id     BIGINT   NULL COMMENT '帖子ID',
    user_id     BIGINT   NULL COMMENT '用户ID',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '帖子点赞记录表';

CREATE TABLE notification
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted  TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-在册 1-已删除',
    user_id     BIGINT       NULL COMMENT '接收用户ID',
    title       VARCHAR(128) NULL COMMENT '通知标题',
    content     VARCHAR(512) NULL COMMENT '通知内容',
    type        TINYINT      NOT NULL DEFAULT 1 COMMENT '通知类型：1-租约到期提醒 2-预约状态变更 3-新预约通知 4-租约状态变更 5-缴费提醒 6-帖子被赞 7-帖子被评论',
    is_read     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
    related_id  BIGINT       NULL COMMENT '关联业务ID',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '通知消息表';
