-- ============================================================
-- V3: 后台管理员账号 + 演示房源数据
-- 管理端登录：admin / admin123（BCrypt，首次登录后请修改）
-- ============================================================

-- 后台管理员
INSERT INTO system_user (id, username, password, name, type, post_id, status)
VALUES (1, 'admin', '$2a$10$2vOtMmeH6kuWNSvlTf8JWuwDdfYywmfIID/hFXgzZi.A.qkakQGlK', '系统管理员', 0, 1, 1);

-- 演示公寓（已发布）
INSERT INTO apartment_info (id, name, introduction, district_id, district_name, city_id, city_name,
                            province_id, province_name, address_detail, latitude, longitude, phone, is_release)
VALUES (1, '硅谷好寓·望京店', '近地铁望京站，拎包入住，公共区域定期保洁，24H安保。', 1, '朝阳区', 1, '北京市',
        1, '北京市', '望京SOHO T1 200米', '39.996', '116.481', '010-88886666', 1);

-- 演示房间（已发布）
INSERT INTO room_info (id, room_number, rent, apartment_id, is_release)
VALUES (1, 'A101', 3500.00, 1, 1),
       (2, 'A102', 4200.00, 1, 1);

-- 公寓关联：配套 + 标签 + 杂费
INSERT INTO apartment_facility (apartment_id, facility_id) VALUES (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6);
INSERT INTO apartment_label (apartment_id, label_id) VALUES (1, 1), (1, 3), (1, 5);
INSERT INTO apartment_fee_value (apartment_id, fee_value_id) VALUES (1, 1), (1, 2), (1, 4);

-- 房间关联：属性 + 配套 + 标签 + 支付方式 + 租期
INSERT INTO room_attr_value (room_id, attr_value_id) VALUES (1, 2), (1, 6), (1, 9), (1, 14);
INSERT INTO room_attr_value (room_id, attr_value_id) VALUES (2, 3), (2, 6), (2, 10), (2, 14);
INSERT INTO room_facility (room_id, facility_id) VALUES (1, 9), (1, 10), (1, 11), (1, 14), (1, 18);
INSERT INTO room_facility (room_id, facility_id) VALUES (2, 9), (2, 10), (2, 12), (2, 17), (2, 19), (2, 20);
INSERT INTO room_label (room_id, label_id) VALUES (1, 6), (1, 8);
INSERT INTO room_label (room_id, label_id) VALUES (2, 7), (2, 9);
INSERT INTO room_payment_type (room_id, payment_type_id) VALUES (1, 1), (1, 2), (2, 1), (2, 2);
INSERT INTO room_lease_term (room_id, lease_term_id) VALUES (1, 1), (1, 3), (2, 2), (2, 3);
