# 栖巢长租公寓管理系统

基于 Spring Boot 3 的短租公寓管理平台，提供后台管理端和用户移动端双端服务。

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0.5-brightgreen)
![MySQL](https://img.shields.io/badge/MySQL-8.x-blue)
![Redis](https://img.shields.io/badge/Redis-7.x-red)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.10-orange)
![License](https://img.shields.io/badge/License-MIT-yellow)

## 特性

- 房源管理：公寓/房间信息维护，支持图片上传
- 租约管理：在线签约、状态流转、自动到期处理
- 预约系统：看房预约、状态跟踪
- 社区圈子：用户发帖点评、点赞、评论互动
- 通知系统：7 类消息通知，异步投递
- 报修工单：租户在线报修、管理端跟进处理
- 公告广播：管理端发布系统公告，MQ 异步推送
- 双端分离：管理员后台 + 用户移动端
- 安全认证：JWT + 图形验证码/邮箱验证码
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

项目采用 Maven 多模块架构，分为以下模块：

```
lease/                           <- 根模块（统一依赖管理）
├── model/                       <- 数据模型层（实体类、枚举）
├── common/                      <- 公共层（工具类、配置、统一返回、异常处理、MQ、邮件）
└── web/                         <- Web 父模块（统一管理 Web 子模块）
    ├── web-admin/               <- 后台管理端（端口 8080）
    └── web-app/                 <- 用户移动端（端口 8081）
```

### 模块说明

| 模块 | 说明 | 技术栈 |
|---|---|---|
| **model** | 数据模型层，定义所有数据库实体类和枚举类型 | JPA 注解 |
| **common** | 公共组件层，统一返回结果、异常处理、工具类、MQ 配置、邮件服务 | JWT, Redis, MinIO, RabbitMQ, Spring Mail |
| **web-admin** | 后台管理系统，管理员操作入口 | Spring Security |
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
- **报修管理**：查看报修工单、按状态/公寓筛选、处理状态流转（待处理/处理中/已完成/已关闭），状态变更自动通知租户
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

- **登录认证**：邮箱验证码登录，JWT 鉴权，新用户自动注册
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

## 前端项目

### 管理端前端 (rentHouseAdmin)

- **技术栈**：Vue 3 + TypeScript + Vite 4 + Element Plus + Pinia + ECharts 5
- **API 连接**：开发环境代理 `/admin` 请求到 `http://localhost:8080`

#### 常用命令

```bash
npm install          # 安装依赖
npm run dev          # 启动开发服务器
npm run build        # 生产构建
npm run lint         # ESLint 检查并自动修复
npm run format       # Prettier 格式化
```

#### 目录结构

```
src/
├── api/            # 按领域组织的 API 接口
├── components/     # 全局组件（ProTable, SearchForm 等）
├── enums/          # 业务枚举和常量映射
├── hooks/          # 组合式函数
├── layouts/        # 布局壳（SideBar, NavBar, TabsBar, Main）
├── router/         # 路由配置
├── store/          # Pinia 状态（user, settings, tabsBar）
├── utils/          # 工具（HTTP 客户端、缓存、事件总线）
└── views/          # 页面组件
```

#### 架构要点

- **状态管理**：Pinia，三个 store 模块 -- `user`（token + 用户信息，持久化）、`settings`（主题/侧边栏）、`tabsBar`（标签页导航）
- **路由**：Hash 模式，静态路由定义在 `src/router/constantRoutes.ts`，导航守卫校验 token
- **HTTP 客户端**：Axios 封装在 `src/utils/http/`，请求拦截器自动附加 `access-token`，响应拦截器处理过期码（305/601/602）
- **ProTable 组件**：核心表格组件，封装了搜索、分页、列配置、数据请求
- **主题系统**：支持暗黑模式和动态主色调，通过 CSS 自定义属性实现

### 用户端前端 (rentHouseH5)

- **技术栈**：Vue 3 + TypeScript + Vite 4 + Vant 4 + Pinia + Tailwind CSS + Less
- **API 连接**：开发环境代理 `/app` 请求到 `http://localhost:8081`
- **应用名称**：桔巢

#### 常用命令

```bash
npm install          # 安装依赖
npm run dev          # 启动开发服务器
npm run build        # 生产构建
npm run type-check   # TypeScript 类型检查
npm run lint         # ESLint 检查并自动修复
```

#### 目录结构

```
src/
├── api/            # API 接口（search/ 核心业务, user/ 认证, community/ 社区）
├── components/     # 自定义组件（RoomCard, ApartmentCard, SearchBar, SvgIcon 等）
├── enums/          # 业务枚举和请求码常量
├── hooks/          # 组合式函数
├── icons/          # 170+ 自定义 SVG 图标
├── layout/         # 主布局（NavBar + keep-alive router-view + Tabbar）
├── router/         # 路由配置（tabBarRoutes + otherRoutes）
├── store/          # Pinia 状态（user, cachedView, darkMode）
├── styles/         # 全局样式（Less + Tailwind，暗黑模式变量）
├── utils/          # 工具（HTTP 客户端、token 管理）
└── views/          # 页面组件
```

#### 架构要点

- **移动端适配**：px-to-viewport PostCSS 插件（375px 设计稿基准），Vant 4 移动端组件库
- **状态管理**：Pinia，三个 store -- `user`（token + 用户信息，持久化）、`cachedView`（keep-alive 缓存管理）、`darkMode`（暗黑模式）
- **路由**：Hash 模式，底部 TabBar 5 个主 tab（找房、圈子、我的房间、消息、个人中心）+ 多个子页面
- **HTTP 客户端**：Axios 封装在 `src/utils/http/`，请求拦截器附加 `access-token`，响应拦截器处理过期码（305/501/601/602）
- **无限滚动**：`PullDownRefreshContainer` 组件基于 IntersectionObserver 实现下拉刷新 + 上拉加载
- **暗黑模式**：CSS 自定义属性 + Vant ConfigProvider 主题切换 + 系统偏好检测
- **keep-alive 缓存**：组件 `name` 需与路由 `name` 一致，`meta.noCache: true` 的页面不缓存
- **统一导航栏**：5 个 Tab 页面共享全局 NavBar（标题"桔巢" + 暗色模式切换）

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

2. **创建数据库**
   ```sql
   CREATE DATABASE lease_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

3. **导入表结构**
   ```bash
   mysql -u username -p lease_management < sql/lease_management.sql
   ```

4. **配置应用**
   修改配置文件中的数据库、Redis、RabbitMQ、MinIO、邮件等连接信息：
   - 后台管理端：`web/web-admin/src/main/resources/application.yml`
   - 用户移动端：`web/web-app/src/main/resources/application.yml`

5. **编译运行后端**
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

6. **运行前端**
   ```bash
   # 管理端前端
   cd rentHouseAdmin
   npm install
   npm run dev

   # 用户端前端
   cd rentHouseH5
   npm install
   npm run dev
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

## API 认证说明

### 后台管理端 (web-admin)

1. 获取图形验证码：`GET /admin/login/captcha`
2. 登录获取 Token：`POST /admin/login`（请求头携带验证码 key）
3. 后续请求在 Header 中携带 `access-token: <JWT>`

### 用户移动端 (web-app)

1. 获取邮箱验证码：`GET /app/login/getCode?email=xxx`
2. 验证码登录获取 Token：`POST /app/login`
3. 后续请求在 Header 中携带 `access-token: <JWT>`

### 前端请求配置

前后端通过 RESTful API 通信，所有请求 Header 携带 `access-token: <JWT>`：

| 前端 | 后端模块 | API 前缀 | 认证方式 |
|---|---|---|---|
| rentHouseAdmin | web-admin | `/admin/` | 图形验证码 + 用户名密码 |
| rentHouseH5 | web-app | `/app/` | 邮箱验证码 |

**响应格式**统一为 `{ code: number, message: string, data: T }`，前端响应拦截器根据 `code` 判断成功/失败/登录过期。

## 核心枚举

### 房源相关

- `ItemType` - 关联类型：APARTMENT(1) 公寓, ROOM(2) 房间
- `BaseStatus` - 基础状态：ENABLE(1) 启用, DISABLE(0) 禁用
- `ReleaseStatus` - 发布状态：RELEASED(1) 已发布, NOT_RELEASED(0) 未发布

### 租约相关

- `LeaseStatus` - 租约状态：SIGNING(1) 签约中, SIGNED(2) 已签约, CANCELED(3) 已取消, EXPIRED(4) 已到期, WITHDRAWING(5) 退租中, WITHDRAWN(6) 已退租, RENEWING(7) 续签中
- `LeaseSourceType` - 租约来源：NEW(1) 新签, RENEW(2) 续签

### 预约相关

- `AppointmentStatus` - 预约状态：WAITING(1) 待看房, CANCELED(2) 已取消, VIEWED(3) 已看房

### 社区相关

- `PostStatus` - 帖子状态：NORMAL(1) 正常, REVIEWING(2) 审核中, BLOCKED(3) 已屏蔽
- `NotificationType` - 通知类型：LEASE_EXPIRY(1), APPOINTMENT_CHANGE(2), NEW_APPOINTMENT(3), LEASE_CHANGE(4), PAYMENT_REMINDER(5), POST_LIKE(6), POST_COMMENT(7)

### 报修相关

- `RepairStatus` - 报修状态：PENDING(0) 待处理, PROCESSING(1) 处理中, COMPLETED(2) 已完成, CLOSED(3) 已关闭

### 用户相关

- `SystemUserType` - 系统用户类型：ADMIN(0) 管理员, COMMON(1) 普通用户

## 贡献

欢迎提交 Issue 和 Pull Request！

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情
