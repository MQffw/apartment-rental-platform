# 房屋租赁管理系统 (Lease Management System)

基于 Spring Boot 3 的房屋租赁管理平台，提供后台管理端和用户移动端双端服务。

## 技术栈

| 组件 | 版本 |
|---|---|
| Spring Boot | 3.0.5 |
| Java | 17 |
| MyBatis-Plus | 3.5.3.1 |
| MySQL | 8.x |
| Redis | 7.x |
| MinIO | 8.2.0 |
| JWT (jjwt) | 0.11.2 |
| Knife4j (API 文档) | 4.1.0 |
| Lombok | 1.18.26 |

## 项目结构

项目采用 Maven 多模块架构，分为以下四个模块：

```
lease/                           ← 根模块（统一依赖管理）
├── model/                       ← 数据模型层（实体类、枚举）
├── common/                      ← 公共层（工具类、配置、统一返回、异常处理）
└── web/                         ← Web 父模块（统一管理 Web 子模块）
    ├── web-admin/               ← 后台管理端（端口 8080）
    └── web-app/                 ← 用户移动端（端口 8081）
```

### 模块说明

- **model**：定义所有数据库实体类（Entity）和枚举类型（Enum），不依赖其他业务模块。
- **common**：封装公共组件，包括统一返回结果 `Result<T>`、全局异常处理、JWT 工具类、Redis 配置、MinIO 文件上传、邮件发送等。
- **web-admin**：后台管理系统，供管理员管理房源、租约、预约、系统用户等。
- **web-app**：用户端应用，供租客浏览房源、预约看房、签署租约等。

## 功能特性

### 后台管理端 (web-admin)

- **登录认证**：图形验证码 + 用户名密码登录，JWT 鉴权
- **房源管理**：公寓/房间的增删改查、上架/下架
- **租约管理**：租约的创建、查询、状态流转（签约、取消、到期、续签）
- **预约管理**：查看看房预约、更新预约状态
- **属性管理**：设施、标签、属性键值、杂费项、租期、付款方式等基础数据维护
- **地区管理**：省市区三级联动数据管理
- **文件上传**：图片上传至 MinIO 对象存储
- **系统管理**：后台用户管理、岗位管理
- **用户管理**：平台用户信息查看与管理
- **定时任务**：每日自动检查并更新到期租约状态

### 用户移动端 (web-app)

- **登录认证**：邮箱验证码登录，JWT 鉴权，新用户自动注册
- **房源浏览**：分页浏览房间列表、查看房间详情
- **公寓浏览**：分页浏览公寓列表、查看公寓详情
- **预约看房**：创建预约、查看我的预约、取消预约
- **租约管理**：查看我的租约、租约详情、签署/取消/续签
- **浏览历史**：记录和查询房源浏览历史
- **基础数据**：地区、付款方式、租期等查询

## 数据库设计

系统包含以下核心数据表：

| 分类 | 表名 | 说明 |
|---|---|---|
| 房源 | `apartment_info` | 公寓信息 |
| 房源 | `room_info` | 房间信息 |
| 房源关系 | `apartment_facility` | 公寓-设施关联 |
| 房源关系 | `apartment_label` | 公寓-标签关联 |
| 房源关系 | `apartment_fee_value` | 公寓-杂费关联 |
| 房源关系 | `room_facility` | 房间-设施关联 |
| 房源关系 | `room_label` | 房间-标签关联 |
| 房源关系 | `room_attr_value` | 房间-属性值关联 |
| 房源关系 | `room_lease_term` | 房间-租期关联 |
| 房源关系 | `room_payment_type` | 房间-付款方式关联 |
| 租约 | `lease_agreement` | 租约信息 |
| 预约 | `view_appointment` | 看房预约 |
| 用户 | `system_user` | 后台系统用户 |
| 用户 | `user_info` | 平台用户 |
| 系统 | `system_post` | 岗位信息 |
| 基础数据 | `facility_info` | 设施信息 |
| 基础数据 | `label_info` | 标签信息 |
| 基础数据 | `attr_key` / `attr_value` | 属性键/值 |
| 基础数据 | `fee_value` | 杂费信息 |
| 基础数据 | `lease_term` | 租期信息 |
| 基础数据 | `payment_type` | 付款方式 |
| 基础数据 | `province_info` / `city_info` / `district_info` | 省市区 |
| 基础数据 | `graph_info` | 图片信息 |
| 其他 | `browsing_history` | 浏览历史 |

## 快速开始

### 环境准备

1. **JDK 17+**
2. **MySQL 8.x** — 创建数据库并导入表结构
3. **Redis 7.x** — 用于登录会话缓存
4. **MinIO** — 用于图片等文件存储

### 配置

修改以下两个配置文件中的数据库、Redis、MinIO、邮件等连接信息：

- 后台管理端：`web/web-admin/src/main/resources/application.yml`
- 用户移动端：`web/web-app/src/main/resources/application.yml`

### 启动

```bash
# 根目录编译打包
mvn clean install

# 启动后台管理端（端口 8080）
cd web/web-admin
mvn spring-boot:run

# 启动用户移动端（端口 8081）
cd web/web-app
mvn spring-boot:run
```

### 访问

| 服务 | 地址 |
|---|---|
| 后台管理端 | http://localhost:8080 |
| 后台 API 文档 | http://localhost:8080/doc.html |
| 用户移动端 | http://localhost:8081 |
| 用户端 API 文档 | http://localhost:8081/doc.html |

## API 认证说明

### 后台管理端

1. 获取图形验证码：`GET /admin/login/captcha`
2. 登录获取 Token：`POST /admin/login`（请求头携带验证码 key）
3. 后续请求在 Header 中携带 `access-token: <JWT>`

### 用户移动端

1. 获取邮箱验证码：`GET /app/login/getCode?email=xxx`
2. 验证码登录获取 Token：`POST /app/login`
3. 后续请求在 Header 中携带 `access-token: <JWT>`

## 核心枚举

| 枚举 | 说明 | 值 |
|---|---|---|
| `ItemType` | 关联类型 | APARTMENT(1) 公寓, ROOM(2) 房间 |
| `BaseStatus` | 基础状态 | ENABLE(1) 启用, DISABLE(0) 禁用 |
| `ReleaseStatus` | 发布状态 | RELEASED(1) 已发布, NOT_RELEASED(0) 未发布 |
| `LeaseStatus` | 租约状态 | SIGNING(1) 签约中, SIGNED(2) 已签约, CANCELED(3) 已取消, EXPIRED(4) 已到期, WITHDRAWING(5) 退租中, WITHDRAWN(6) 已退租, RENEWING(7) 续签中 |
| `LeaseSourceType` | 租约来源 | NEW(1) 新签, RENEW(2) 续签 |
| `AppointmentStatus` | 预约状态 | WAITING(1) 待看房, CANCELED(2) 已取消, VIEWED(3) 已看房 |
| `SystemUserType` | 系统用户类型 | ADMIN(0) 管理员, COMMON(1) 普通用户 |
