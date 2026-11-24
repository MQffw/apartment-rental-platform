# 栖巢长租公寓管理系统

基于 Spring Boot 3 的长租公寓管理平台，提供后台管理端和用户移动端双端服务。

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0.5-brightgreen)
![MySQL](https://img.shields.io/badge/MySQL-8.x-blue)
![Redis](https://img.shields.io/badge/Redis-7.x-red)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.10-orange)
![License](https://img.shields.io/badge/License-MIT-yellow)

## 文档导航

| 文档 | 用途 |
|---|---|
| [AGENTS.md](./AGENTS.md) | 开发者 / AI 编码指南：模块依赖、架构模式、认证流程、前后端技术细节（**唯一维护副本**） |
| [docs/wiki/](./docs/wiki/README.md) | 模块级深度文档：实体与表结构、各模块 API 与 Service 逻辑、MQ 与定时任务、部署指南 |
| [docs/wiki/09-运行与部署指南.md](./docs/wiki/09-运行与部署指南.md) | 环境准备、中间件配置、构建启动、常见问题 |
| [frontend/rentHouseAdmin/README.md](./frontend/rentHouseAdmin/README.md) | 管理端前端自身说明 |
| [frontend/rentHouseH5/README.md](./frontend/rentHouseH5/README.md) | 移动端前端自身说明 |

## 特性

- 房源管理：公寓/房间信息维护，支持图片上传
- 租约管理：在线签约、状态流转、自动到期处理
- 预约系统：看房预约、状态跟踪
- 社区圈子：用户发帖点评、点赞、评论互动
- 通知系统：7 类消息通知，异步投递
- 报修工单：租户在线报修、管理端跟进处理
- 公告广播：管理端发布系统公告，MQ 异步推送
- 双端分离：管理员后台 + 用户移动端
- 安全认证：JWT + 图形验证码/邮箱验证码，token 带 scope 做跨端隔离
- 数据可视化：Dashboard 统计面板 + ECharts 图表

## 技术栈

| 组件 | 版本 |
|---|---|
| Spring Boot | 3.0.5 |
| Java | 17 |
| MyBatis-Plus | 3.5.3.1 |
| MySQL | 8.x |
| Redis | 7.x |
| RabbitMQ | 3.10 |
| MinIO | 8.2.0 |
| JWT (jjwt) | 0.11.2 |
| Knife4j (API 文档) | 4.1.0 |
| Lombok | 1.18.26 |

## 项目结构

项目采用 Maven 多模块架构：

```
lease/                           <- 根模块（统一依赖管理）
├── model/                       <- 数据模型层（35 个实体类、14 个枚举）
├── common/                      <- 公共层（工具类、配置、统一返回、异常处理、MQ、邮件）
├── web/                         <- Web 父模块（统一管理 Web 子模块）
│   ├── web-admin/               <- 后台管理端（端口 8080，20 个控制器）
│   └── web-app/                 <- 用户移动端（端口 8081，16 个控制器）
├── frontend/
│   ├── rentHouseAdmin/          <- 管理端前端（Vue 3 + Element Plus）
│   └── rentHouseH5/             <- 用户端 H5（Vue 3 + Vant 4，应用名「栖巢」）
└── docs/wiki/                   <- 模块级深度文档
```

### 模块说明

| 模块 | 说明 | 技术栈 |
|---|---|---|
| **model** | 数据模型层，定义所有数据库实体类和枚举类型 | JPA 注解 |
| **common** | 公共组件层，统一返回结果、异常处理、工具类、MQ 配置、邮件服务 | JWT, Redis, MinIO, RabbitMQ, Spring Mail |
| **web-admin** | 后台管理系统，管理员操作入口 | Spring MVC + 自定义拦截器 |
| **web-app** | 用户移动端，租客使用入口 | JWT 认证 |

### 架构图

```
用户端 (web-app) <-- API --> 控制器层
     |                         |
管理员端 (web-admin) <-- API --> 控制器层
     |                         |
   服务层 (Service)
     |
   数据访问层 (MyBatis-Plus)
     |
   数据库 (MySQL) + 缓存 (Redis) + 文件存储 (MinIO)
     |
   消息队列 (RabbitMQ) --> 异步通知 / 帖子入库 / 点赞同步 / 评论入库
```

## 功能特性

### 后台管理端 (web-admin)

- **登录认证**：图形验证码 + 用户名密码登录，JWT 鉴权
- **Dashboard 首页**：公寓/房间/用户统计、租约状态分布、入住率、房间饼图、月度趋势图、预约统计
- **房源管理**：公寓/房间的增删改查、上架/下架
- **租约管理**：租约的创建、查询、状态流转（签约、取消、到期、续签）
- **预约管理**：查看看房预约、更新预约状态
- **报修管理**：查看报修工单、按状态/公寓筛选、状态流转（待处理/处理中/已完成/已关闭），状态变更自动通知租户
- **帖子管理**：查看帖子列表、屏蔽/恢复/删除帖子、帖子数据统计
- **评论管理**：查看评论列表、删除违规评论
- **公告发送**：面向全体用户或指定用户发布系统公告，通过 MQ 异步投递
- **属性管理**：设施、标签、属性键值、杂费项、租期、付款方式等基础数据维护
- **地区管理**：省市区三级联动数据管理
- **文件上传**：图片上传至 MinIO 对象存储
- **系统管理**：后台用户管理（CRUD + 密码加密）、岗位管理
- **用户管理**：平台用户信息查看与管理
- **定时任务**：每日自动检查并更新到期租约状态

### 用户移动端 (web-app)

- **登录认证**：邮箱验证码登录，支持密码登录，新用户自动注册
- **房源浏览**：分页浏览房间列表、查看房间详情、多维度筛选搜索
- **公寓浏览**：分页浏览公寓列表、查看公寓详情
- **我的房间**：查看当前租约关联的房间信息、租约状态展示
- **预约看房**：创建预约、查看我的预约、取消预约
- **租约管理**：查看我的租约、租约详情、签署/取消/续签
- **社区圈子**：浏览帖子动态、发帖点评（支持图片和评分）、点赞/评论互动、热门排序
- **消息通知**：7 类通知（租约到期、预约变更、新预约、租约变更、缴费提醒、点赞通知、评论通知），全部已读、单条已读
- **报修申请**：在线提交报修工单（自动关联当前租约房间）、查看报修进度
- **个人中心**：用户资料编辑、密码设置、暗色模式切换
- **浏览历史**：记录和查询房源浏览历史
- **基础数据**：地区、付款方式、租期等查询

## 消息队列设计

系统使用 RabbitMQ 实现关键业务的异步解耦，共 4 条队列（均配备死信队列）：

| 队列 | 用途 | 说明 |
|---|---|---|
| `notification.queue` | 通知投递 | 异步创建各类通知消息 |
| `post.create.queue` | 帖子入库 | 发帖请求异步入库，削峰 |
| `post.like.queue` | 点赞同步 | 点赞/取消点赞异步写入 DB |
| `post.comment.queue` | 评论入库 | 评论异步持久化 + 通知帖子作者 |

所有队列使用手动 ACK 模式，prefetch=10，Jackson JSON 消息转换器。

## Redis 高级用法

| 场景 | 实现方式 |
|---|---|
| 帖子点赞 | Redis Set 记录用户点赞 + Lua 脚本原子操作计数 |
| 频率限制 | 发帖 1/60s、评论 3/10s、点赞 10/1s |
| 帖子缓存 | 详情缓存 5min TTL、列表缓存 5min TTL |
| 热门排行 | Sorted Set `post:hot:rank` |
| 通知未读数 | 缓存未读计数 |
| 邮箱验证码 | 验证码存储，可配置长度/TTL/重发间隔 |

## 数据库设计

系统包含以下数据表：

### 房源相关

| 表名 | 说明 |
|---|---|
| `apartment_info` | 公寓信息 |
| `room_info` | 房间信息 |
| `apartment_facility` | 公寓-设施关联 |
| `apartment_label` | 公寓-标签关联 |
| `room_facility` | 房间-设施关联 |
| `room_label` | 房间-标签关联 |
| `graph_info` | 图片信息 |

### 业务相关

| 表名 | 说明 |
|---|---|
| `lease_agreement` | 租约信息 |
| `view_appointment` | 看房预约 |
| `browsing_history` | 浏览历史 |
| `repair_request` | 报修工单 |

### 社区相关

| 表名 | 说明 |
|---|---|
| `post_info` | 帖子信息（支持图片、评分、点赞/评论计数） |
| `post_comment` | 评论（支持嵌套回复，parentId + replyToId） |
| `post_like` | 点赞记录（userId + postId 唯一） |
| `notification` | 通知消息（7 种类型，已读/未读状态） |

### 用户相关

| 表名 | 说明 |
|---|---|
| `system_user` | 后台系统用户 |
| `user_info` | 平台用户 |
| `system_post` | 岗位信息 |

### 基础数据

| 表名 | 说明 |
|---|---|
| `facility_info` | 设施信息 |
| `label_info` | 标签信息 |
| `attr_key` / `attr_value` | 属性键/值 |
| `fee_value` | 杂费信息 |
| `lease_term` | 租期信息 |
| `payment_type` | 付款方式 |
| `province_info` / `city_info` / `district_info` | 省市区 |

## 快速开始

### 环境要求

- JDK 17+
- MySQL 8.x
- Redis 7.x
- RabbitMQ 3.10+
- MinIO 8.2.0+
- Node.js 18+（前端项目需要）

### 安装步骤

1. **克隆项目**

   ```bash
   git clone https://github.com/yourusername/lease-management-system.git
   cd lease-management-system
   ```

2. **创建空库**（表结构由 Flyway 自动迁移，无需手工导入）

   ```sql
   CREATE DATABASE lease DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

3. **配置密钥**
   复制仓库根目录 `.env.properties.example` 为 `.env.properties`，按模板填入真实值（该文件已被 `.gitignore` 排除）：

   - `DB_PASSWORD` — MySQL 密码（`application-dev.yml` 中无默认值，缺失会导致启动失败）
   - `RABBITMQ_USER`（默认 `admin`）/ `RABBITMQ_PASS` — RabbitMQ 账号
   - `MINIO_ACCESS_KEY`（默认 `minioadmin`）/ `MINIO_SECRET_KEY` — 对象存储
   - 邮件、JWT 等其他敏感项的键名以 `.env.properties.example` 为准

   两个 web 模块通过 `spring.config.import` 读取该文件；数据库、Redis、RabbitMQ、MinIO 地址等在 `application-dev.yml` / `application-prod.yml` 中调整。

4. **初始化表结构**
   无需手工执行 SQL。`web-admin` 内置 Flyway，启动时自动按 `src/main/resources/db/migration` 下的 `V1__init_schema` → `V2__init_dict` → `V3__init_admin_and_demo` 顺序建表并写入字典与演示数据（`baseline-version=3`，已有旧库会跳过前三版）。`web-app` 不引入 Flyway，仅连接同一库。

5. **编译运行后端**

   ```bash
   mvn clean install

   cd web/web-admin && mvn spring-boot:run    # 管理端，8080（首次启动执行迁移）
   cd web/web-app   && mvn spring-boot:run    # 用户端，8081
   ```

6. **运行前端**

   ```bash
   cd frontend/rentHouseAdmin && npm install && npm run dev
   cd frontend/rentHouseH5    && npm install && npm run dev
   ```

### 访问地址

| 服务 | 地址 | 说明 |
|---|---|---|
| 后台管理端 API | http://localhost:8080 | 管理端后端 |
| 后台管理端前端 | http://localhost:5173 | 管理端前端（Vite 开发服务器） |
| 后台 API 文档 | http://localhost:8080/doc.html | Knife4j 文档 |
| 用户移动端 API | http://localhost:8081 | 用户端后端 |
| 用户移动端前端 | http://localhost:5174 | 用户端前端（Vite 开发服务器） |
| 用户端 API 文档 | http://localhost:8081/doc.html | Knife4j 文档 |

## 认证与核心枚举

认证流程（JWT + `access-token` 请求头，跨端 scope 隔离）、核心枚举取值、前后端通信约定，统一见 [AGENTS.md](./AGENTS.md)。

## 贡献

欢迎提交 Issue 和 Pull Request！

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情
