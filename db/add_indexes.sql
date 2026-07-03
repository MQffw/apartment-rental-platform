-- ============================================================
-- lease 数据库索引添加脚本
-- 生成日期: 2026-07-02
-- 说明: 所有索引均使用 ALTER TABLE ADD INDEX，重复执行会报"重复索引名"错误，
--       建议首次执行前确认索引不存在
-- ============================================================

-- ----------------------------
-- 1. lease_agreement（租约信息表）
-- 当前无任何二级索引，查询压力最大的表之一
-- ----------------------------

-- 复合索引: 定时任务 checkLeaseStatus 每天执行
-- WHERE status IN (SIGNED, WITHDRAWING) AND lease_end_date <= NOW()
ALTER TABLE `lease_agreement`
    ADD INDEX `idx_status_lease_end_date` (`status`, `lease_end_date`) USING BTREE;

-- 单列索引: 按公寓查租约、按房间查租约
-- 注: status 单列查询已被 idx_status_lease_end_date 的左前缀覆盖，无需单独建索引
ALTER TABLE `lease_agreement`
    ADD INDEX `idx_apartment_id` (`apartment_id`) USING BTREE,
    ADD INDEX `idx_room_id` (`room_id`) USING BTREE;

-- ----------------------------
-- 2. browsing_history（浏览历史）
-- 无二级索引，app 端分页查询浏览历史会全表扫描
-- ----------------------------
ALTER TABLE `browsing_history`
    ADD INDEX `idx_user_id` (`user_id`, `create_time` DESC) USING BTREE,
    ADD INDEX `idx_room_id` (`room_id`) USING BTREE;

-- ----------------------------
-- 3. graph_info（图片信息表）
-- 无二级索引，按 item_type+item_id 查询图片列表是高频操作
-- ----------------------------
ALTER TABLE `graph_info`
    ADD INDEX `idx_item_type_item_id` (`item_type`, `item_id`) USING BTREE;

-- ----------------------------
-- 4. apartment_info（公寓信息表）
-- 无二级索引，按城市/区域筛选公寓是常见场景
-- ----------------------------
ALTER TABLE `apartment_info`
    ADD INDEX `idx_city_id` (`city_id`) USING BTREE,
    ADD INDEX `idx_district_id` (`district_id`) USING BTREE,
    ADD INDEX `idx_is_release` (`is_release`) USING BTREE;

-- ----------------------------
-- 5. room_info（房间信息表）
-- 无二级索引，按公寓查房间、按发布状态筛选
-- ----------------------------
ALTER TABLE `room_info`
    ADD INDEX `idx_apartment_id` (`apartment_id`) USING BTREE,
    ADD INDEX `idx_is_release` (`is_release`) USING BTREE;

-- ----------------------------
-- 6. view_appointment（预约看房表）
-- 无二级索引，按用户查预约、按公寓查预约
-- ----------------------------
ALTER TABLE `view_appointment`
    ADD INDEX `idx_user_id` (`user_id`) USING BTREE,
    ADD INDEX `idx_apartment_id` (`apartment_id`) USING BTREE,
    ADD INDEX `idx_appointment_status` (`appointment_status`) USING BTREE;

-- ----------------------------
-- 7. system_user（员工表）
-- 无二级索引，登录时按 username 查询
-- ----------------------------
ALTER TABLE `system_user`
    ADD INDEX `idx_username` (`username`) USING BTREE,
    ADD INDEX `idx_post_id` (`post_id`) USING BTREE;

-- ----------------------------
-- 8. user_info（用户表）
-- 登录时按 phone 查询（email 已有 uk_email 唯一索引，无需重复）
-- ----------------------------
ALTER TABLE `user_info`
    ADD INDEX `idx_phone` (`phone`) USING BTREE;

-- ----------------------------
-- 9. 关联表索引（FK 字段）
-- 这些表在删除公寓/房间时用于级联查询和删除
-- ----------------------------

-- 公寓关联表
ALTER TABLE `apartment_facility`
    ADD INDEX `idx_apartment_id` (`apartment_id`) USING BTREE;

ALTER TABLE `apartment_fee_value`
    ADD INDEX `idx_apartment_id` (`apartment_id`) USING BTREE;

ALTER TABLE `apartment_label`
    ADD INDEX `idx_apartment_id` (`apartment_id`) USING BTREE;

-- 房间关联表
ALTER TABLE `room_attr_value`
    ADD INDEX `idx_room_id` (`room_id`) USING BTREE;

ALTER TABLE `room_facility`
    ADD INDEX `idx_room_id` (`room_id`) USING BTREE;

ALTER TABLE `room_label`
    ADD INDEX `idx_room_id` (`room_id`) USING BTREE;

ALTER TABLE `room_lease_term`
    ADD INDEX `idx_room_id` (`room_id`) USING BTREE;

ALTER TABLE `room_payment_type`
    ADD INDEX `idx_room_id` (`room_id`) USING BTREE;

-- ----------------------------
-- 10. 其他表 FK 索引
-- ----------------------------

ALTER TABLE `city_info`
    ADD INDEX `idx_province_id` (`province_id`) USING BTREE;

ALTER TABLE `district_info`
    ADD INDEX `idx_city_id` (`city_id`) USING BTREE;

ALTER TABLE `fee_value`
    ADD INDEX `idx_fee_key_id` (`fee_key_id`) USING BTREE;

ALTER TABLE `attr_value`
    ADD INDEX `idx_attr_key_id` (`attr_key_id`) USING BTREE;

-- payment_record 补充 payment_status 索引，用于查询待支付记录
ALTER TABLE `payment_record`
    ADD INDEX `idx_payment_status` (`payment_status`) USING BTREE;
