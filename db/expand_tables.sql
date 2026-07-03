-- ================================================================
-- Lease 项目功能扩展 - 新增表 + 索引 + 字段扩展
-- ================================================================

-- 1. notification 表添加索引
ALTER TABLE notification ADD INDEX idx_user_read (user_id, is_read);
ALTER TABLE notification ADD INDEX idx_create_time (create_time);

-- 2. payment_record 表新增 bill_type 字段（1-房租 2-电费 3-水费 4-物业费）
ALTER TABLE payment_record ADD COLUMN bill_type TINYINT DEFAULT 1 COMMENT '1-房租 2-电费 3-水费 4-物业费' AFTER payment_status;

-- 3. repair_request（报修工单表）
CREATE TABLE IF NOT EXISTS repair_request (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT NOT NULL COMMENT '用户ID',
    room_id      BIGINT NOT NULL COMMENT '房间ID',
    apartment_id BIGINT NOT NULL COMMENT '公寓ID',
    title        VARCHAR(100) COMMENT '报修标题',
    description  TEXT COMMENT '问题描述',
    images       VARCHAR(1000) COMMENT '图片URL,逗号分隔',
    status       TINYINT DEFAULT 0 COMMENT '0-待处理 1-处理中 2-已完成 3-已关闭',
    create_time  DATETIME,
    update_time  DATETIME,
    is_deleted   TINYINT DEFAULT 0,
    INDEX idx_user (user_id),
    INDEX idx_room (room_id),
    INDEX idx_apartment (apartment_id)
) COMMENT '报修工单表';

-- 4. post_info（帖子/点评表）
CREATE TABLE IF NOT EXISTS post_info (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT NOT NULL COMMENT '发帖人 ID',
    apartment_id  BIGINT COMMENT '关联公寓ID',
    room_id       BIGINT COMMENT '关联房间ID',
    title         VARCHAR(100) NOT NULL COMMENT '标题',
    content       TEXT NOT NULL COMMENT '正文',
    images        VARCHAR(2000) COMMENT '图片URL,JSON数组',
    rating        TINYINT COMMENT '评分 1-5',
    like_count    INT DEFAULT 0 COMMENT '点赞数（定时从Redis回写）',
    comment_count INT DEFAULT 0 COMMENT '评论数（定时从Redis回写）',
    status        TINYINT DEFAULT 1 COMMENT '1-正常 2-审核中 3-已屏蔽',
    create_time   DATETIME,
    update_time   DATETIME,
    is_deleted    TINYINT DEFAULT 0,
    INDEX idx_user (user_id),
    INDEX idx_apartment (apartment_id),
    INDEX idx_create (create_time),
    INDEX idx_like (like_count)
) COMMENT '帖子/点评表';

-- 5. post_comment（评论表）
CREATE TABLE IF NOT EXISTS post_comment (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id      BIGINT NOT NULL COMMENT '帖子ID',
    user_id      BIGINT NOT NULL COMMENT '评论人 ID',
    parent_id    BIGINT DEFAULT 0 COMMENT '父评论ID（0=一级）',
    reply_to_id  BIGINT DEFAULT 0 COMMENT '回复目标用户ID',
    content      VARCHAR(500) NOT NULL COMMENT '评论内容',
    like_count   INT DEFAULT 0 COMMENT '点赞数',
    status       TINYINT DEFAULT 1 COMMENT '1-正常 3-已删除',
    create_time  DATETIME,
    update_time  DATETIME,
    is_deleted   TINYINT DEFAULT 0,
    INDEX idx_post (post_id),
    INDEX idx_user (user_id),
    INDEX idx_parent (parent_id)
) COMMENT '帖子评论表';

-- 6. post_like（点赞记录表，日常走Redis，定时批量回写）
CREATE TABLE IF NOT EXISTS post_like (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id     BIGINT NOT NULL,
    user_id     BIGINT NOT NULL,
    create_time DATETIME,
    update_time DATETIME,
    is_deleted  TINYINT DEFAULT 0,
    UNIQUE KEY uk_post_user (post_id, user_id),
    INDEX idx_post (post_id),
    INDEX idx_user (user_id)
) COMMENT '帖子点赞记录表';
