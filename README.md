# 🏠 房屋租赁管理系统 (Lease Management System)

基于 Spring Boot 3 的房屋租赁管理平台，提供后台管理端和用户移动端双端服务。

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0.5-brightgreen)# 🏠 房屋租赁管理系统 (Lease Management System)

基于 Spring Boot 3 的房屋租赁管理平台，提供后台管理端和用户移动端双端服务。

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0.5-brightgreen)
![MySQL](https://img.shields.io/badge/MySQL-8.x-blue)
![Redis](https://img.shields.io/badge/Redis-7.x-red)
![License](https://img.shields.io/badge/License-MIT-yellow)

## ✨ 特性

- 🏠 房源管理：公寓/房间信息维护，支持图片上传
- 📝 租约管理：在线签约、状态流转、自动到期处理
- 📅 预约系统：看房预约、状态跟踪
- 👥 双端分离：管理员后台 + 用户移动端
- 🔐 安全认证：JWT + 图形验证码/邮箱验证码
- 📊 数据可视化：基础数据统计分析

## 🛠️ 技术栈

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

## 🏗️ 项目结构

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

| 模块 | 说明 | 技术栈 |
|---|---|---|
| **model** | 数据模型层，定义所有数据库实体类和枚举类型 | JPA注解 |
| **common** | 公共组件层，统一返回结果、异常处理、工具类 | JWT, Redis, MinIO |
| **web-admin** | 后台管理系统，管理员操作入口 | Spring Security |
| **web-app** | 用户移动端，租客使用入口 | JWT认证 |

### 架构图

```
用户端 (web-app) ← API → 控制器层
     ↓                      ↓
管理员端 (web-admin) ← API → 控制器层
     ↓                      ↓
   服务层 (Service)
     ↓
   数据访问层 (MyBatis-Plus)
     ↓
   数据库 (MySQL) + 缓存 (Redis) + 文件存储 (MinIO)
```

## ✨ 功能特性

### 后台管理端 (web-admin)

- **登录认证**：图形验证码 + 用户名密码登录，JWT 鉴权
- **首页概览**：欢迎信息、数据统计展示
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

## 🌐 前端项目

### 管理端前端 (rentHouseAdmin)

- **路径**：`C:\Users\mq\Desktop\rentHouseAdmin`
- **技术栈**：Vue 3 + TypeScript + Vite 4 + Element Plus + Pinia
- **API 连接**：开发环境代理 `/admin` 请求到 `http://localhost:8080`

#### 常用命令

```bash
cd C:\Users\mq\Desktop\rentHouseAdmin
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

- **状态管理**：Pinia，三个 store 模块 — `user`（token + 用户信息，持久化）、`settings`（主题/侧边栏）、`tabsBar`（标签页导航）
- **路由**：Hash 模式，静态路由定义在 `src/router/constantRoutes.ts`，导航守卫校验 token
- **HTTP 客户端**：Axios 封装在 `src/utils/http/`，请求拦截器自动附加 `access-token`，响应拦截器处理过期码（305/601/602）
- **ProTable 组件**：核心表格组件，封装了搜索、分页、列配置、数据请求
- **主题系统**：支持暗黑模式和动态主色调，通过 CSS 自定义属性实现

### 用户端前端 (rentHouseH5)

- **路径**：`C:\Users\mq\Desktop\rentHouseH5`
- **技术栈**：Vue 3 + TypeScript + Vite 4 + Vant 4 + Pinia + Tailwind CSS
- **API 连接**：开发环境代理 `/app` 请求到 `http://localhost:8081`
- **应用名称**：硅谷租房

#### 常用命令

```bash
cd C:\Users\mq\Desktop\rentHouseH5
npm install          # 安装依赖
npm run dev          # 启动开发服务器
npm run build        # 生产构建
npm run type-check   # TypeScript 类型检查
npm run lint         # ESLint 检查并自动修复
```

#### 目录结构

```
src/
├── api/            # API 接口（search/ 核心业务, user/ 认证）
├── components/     # 自定义组件（RoomCard, ApartmentCard, SearchBar 等）
├── enums/          # 业务枚举和请求码常量
├── hooks/          # 组合式函数
├── icons/          # 60+ SVG 图标
├── layout/         # 主布局（NavBar + keep-alive router-view + Tabbar）
├── router/         # 路由配置（tabBarRoutes + otherRoutes）
├── store/          # Pinia 状态（user, cachedView, darkMode）
├── styles/         # 全局样式（Less + Tailwind，暗黑模式变量）
├── utils/          # 工具（HTTP 客户端、token 管理）
└── views/          # 页面组件
```

#### 架构要点

- **移动端适配**：px-to-viewport PostCSS 插件（375px 设计稿基准），Vant 4 移动端组件库
- **状态管理**：Pinia，三个 store — `user`（token + 用户信息，持久化）、`cachedView`（keep-alive 缓存管理）、`darkMode`（暗黑模式）
- **路由**：Hash 模式，底部 TabBar 5 个主 tab（找房、圈子、我的房间、消息、个人中心）+ 8 个子页面
- **HTTP 客户端**：Axios 封装在 `src/utils/http/`，请求拦截器附加 `access-token`，响应拦截器处理过期码（305/501/601/602）
- **无限滚动**：`PullDownRefreshContainer` 组件基于 IntersectionObserver 实现下拉刷新 + 上拉加载
- **暗黑模式**：CSS 自定义属性 + Vant ConfigProvider 主题切换 + 系统偏好检测
- **keep-alive 缓存**：组件 `name` 需与路由 `name` 一致，`meta.noCache: true` 的页面不缓存

## 💾 数据库设计

系统包含以下核心数据表：

### 🏠 房源相关
- `apartment_info` - 公寓信息
- `room_info` - 房间信息
- `apartment_facility` - 公寓-设施关联
- `apartment_label` - 公寓-标签关联
- `room_facility` - 房间-设施关联
- `room_label` - 房间-标签关联
- `graph_info` - 图片信息

### 📝 业务相关
- `lease_agreement` - 租约信息
- `view_appointment` - 看房预约
- `browsing_history` - 浏览历史

### 👥 用户相关
- `system_user` - 后台系统用户
- `user_info` - 平台用户
- `system_post` - 岗位信息

### ⚙️ 基础数据
- `facility_info` - 设施信息
- `label_info` - 标签信息
- `attr_key` / `attr_value` - 属性键/值
- `fee_value` - 杂费信息
- `lease_term` - 租期信息
- `payment_type` - 付款方式
- `province_info` / `city_info` / `district_info` - 省市区

## 🚀 快速开始

### 环境要求

- JDK 17+
- MySQL 8.x
- Redis 7.x
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
   修改配置文件中的数据库、Redis、MinIO、邮件等连接信息：
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
   cd C:\Users\mq\Desktop\rentHouseAdmin
   npm install
   npm run dev

   # 用户端前端
   cd C:\Users\mq\Desktop\rentHouseH5
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

## 🔐 API 认证说明

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

## 📋 核心枚举

### 🏠 房源相关
- `ItemType` - 关联类型：APARTMENT(1) 公寓, ROOM(2) 房间
- `BaseStatus` - 基础状态：ENABLE(1) 启用, DISABLE(0) 禁用
- `ReleaseStatus` - 发布状态：RELEASED(1) 已发布, NOT_RELEASED(0) 未发布

### 📄 租约相关
- `LeaseStatus` - 租约状态：SIGNING(1) 签约中, SIGNED(2) 已签约, CANCELED(3) 已取消, EXPIRED(4) 已到期, WITHDRAWING(5) 退租中, WITHDRAWN(6) 已退租, RENEWING(7) 续签中
- `LeaseSourceType` - 租约来源：NEW(1) 新签, RENEW(2) 续签

### 📅 预约相关
- `AppointmentStatus` - 预约状态：WAITING(1) 待看房, CANCELED(2) 已取消, VIEWED(3) 已看房

### 👤 用户相关
- `SystemUserType` - 系统用户类型：ADMIN(0) 管理员, COMMON(1) 普通用户

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📞 联系方式

- 项目维护者：[Your Name](mailto:your.email@example.com)
- 项目链接：[https://github.com/yourusername/lease-management-system](https://github.com/yourusername/lease-management-system)
![MySQL](https://img.shields.io/badge/MySQL-8.x-blue)
![Redis](https://img.shields.io/badge/Redis-7.x-red)
![License](https://img.shields.io/badge/License-MIT-yellow)

## ✨ 特性

- 🏠 房源管理：公寓/房间信息维护，支持图片上传
- 📝 租约管理：在线签约、状态流转、自动到期处理
- 📅 预约系统：看房预约、状态跟踪
- 👥 双端分离：管理员后台 + 用户移动端
- 🔐 安全认证：JWT + 图形验证码/邮箱验证码
- 📊 数据可视化：基础数据统计分析

## 🛠️ 技术栈

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

## 🏗️ 项目结构

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

| 模块 | 说明 | 技术栈 |
|---|---|---|
| **model** | 数据模型层，定义所有数据库实体类和枚举类型 | JPA注解 |
| **common** | 公共组件层，统一返回结果、异常处理、工具类 | JWT, Redis, MinIO |
| **web-admin** | 后台管理系统，管理员操作入口 | Spring Security |
| **web-app** | 用户移动端，租客使用入口 | JWT认证 |

### 架构图

```
用户端 (web-app) ← API → 控制器层
     ↓                      ↓
管理员端 (web-admin) ← API → 控制器层
     ↓                      ↓
   服务层 (Service)
     ↓
   数据访问层 (MyBatis-Plus)
     ↓
   数据库 (MySQL) + 缓存 (Redis) + 文件存储 (MinIO)
```

## ✨ 功能特性

### 后台管理端 (web-admin)

- **登录认证**：图形验证码 + 用户名密码登录，JWT 鉴权
- **首页概览**：欢迎信息、数据统计展示
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

## 🌐 前端项目

### 管理端前端 (rentHouseAdmin)

- **路径**：`C:\Users\mq\Desktop\rentHouseAdmin`
- **技术栈**：Vue 3 + TypeScript + Vite 4 + Element Plus + Pinia
- **API 连接**：开发环境代理 `/admin` 请求到 `http://localhost:8080`

#### 常用命令

```bash
cd C:\Users\mq\Desktop\rentHouseAdmin
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

- **状态管理**：Pinia，三个 store 模块 — `user`（token + 用户信息，持久化）、`settings`（主题/侧边栏）、`tabsBar`（标签页导航）
- **路由**：Hash 模式，静态路由定义在 `src/router/constantRoutes.ts`，导航守卫校验 token
- **HTTP 客户端**：Axios 封装在 `src/utils/http/`，请求拦截器自动附加 `access-token`，响应拦截器处理过期码（305/601/602）
- **ProTable 组件**：核心表格组件，封装了搜索、分页、列配置、数据请求
- **主题系统**：支持暗黑模式和动态主色调，通过 CSS 自定义属性实现

### 用户端前端 (rentHouseH5)

- **路径**：`C:\Users\mq\Desktop\rentHouseH5`
- **技术栈**：Vue 3 + TypeScript + Vite 4 + Vant 4 + Pinia + Tailwind CSS
- **API 连接**：开发环境代理 `/app` 请求到 `http://localhost:8081`
- **应用名称**：硅谷租房

#### 常用命令

```bash
cd C:\Users\mq\Desktop\rentHouseH5
npm install          # 安装依赖
npm run dev          # 启动开发服务器
npm run build        # 生产构建
npm run type-check   # TypeScript 类型检查
npm run lint         # ESLint 检查并自动修复
```

#### 目录结构

```
src/
├── api/            # API 接口（search/ 核心业务, user/ 认证）
├── components/     # 自定义组件（RoomCard, ApartmentCard, SearchBar 等）
├── enums/          # 业务枚举和请求码常量
├── hooks/          # 组合式函数
├── icons/          # 60+ SVG 图标
├── layout/         # 主布局（NavBar + keep-alive router-view + Tabbar）
├── router/         # 路由配置（tabBarRoutes + otherRoutes）
├── store/          # Pinia 状态（user, cachedView, darkMode）
├── styles/         # 全局样式（Less + Tailwind，暗黑模式变量）
├── utils/          # 工具（HTTP 客户端、token 管理）
└── views/          # 页面组件
```

#### 架构要点

- **移动端适配**：px-to-viewport PostCSS 插件（375px 设计稿基准），Vant 4 移动端组件库
- **状态管理**：Pinia，三个 store — `user`（token + 用户信息，持久化）、`cachedView`（keep-alive 缓存管理）、`darkMode`（暗黑模式）
- **路由**：Hash 模式，底部 TabBar 5 个主 tab（找房、圈子、我的房间、消息、个人中心）+ 8 个子页面
- **HTTP 客户端**：Axios 封装在 `src/utils/http/`，请求拦截器附加 `access-token`，响应拦截器处理过期码（305/501/601/602）
- **无限滚动**：`PullDownRefreshContainer` 组件基于 IntersectionObserver 实现下拉刷新 + 上拉加载
- **暗黑模式**：CSS 自定义属性 + Vant ConfigProvider 主题切换 + 系统偏好检测
- **keep-alive 缓存**：组件 `name` 需与路由 `name` 一致，`meta.noCache: true` 的页面不缓存

## 💾 数据库设计

系统包含以下核心数据表：

### 🏠 房源相关
- `apartment_info` - 公寓信息
- `room_info` - 房间信息
- `apartment_facility` - 公寓-设施关联
- `apartment_label` - 公寓-标签关联
- `room_facility` - 房间-设施关联
- `room_label` - 房间-标签关联
- `graph_info` - 图片信息

### 📝 业务相关
- `lease_agreement` - 租约信息
- `view_appointment` - 看房预约
- `browsing_history` - 浏览历史

### 👥 用户相关
- `system_user` - 后台系统用户
- `user_info` - 平台用户
- `system_post` - 岗位信息

### ⚙️ 基础数据
- `facility_info` - 设施信息
- `label_info` - 标签信息
- `attr_key` / `attr_value` - 属性键/值
- `fee_value` - 杂费信息
- `lease_term` - 租期信息
- `payment_type` - 付款方式
- `province_info` / `city_info` / `district_info` - 省市区

## 🚀 快速开始

### 环境要求

- JDK 17+
- MySQL 8.x
- Redis 7.x
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
   修改配置文件中的数据库、Redis、MinIO、邮件等连接信息：
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
   cd C:\Users\mq\Desktop\rentHouseAdmin
   npm install
   npm run dev

   # 用户端前端
   cd C:\Users\mq\Desktop\rentHouseH5
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

## 🔐 API 认证说明

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

## 📋 核心枚举

### 🏠 房源相关
- `ItemType` - 关联类型：APARTMENT(1) 公寓, ROOM(2) 房间
- `BaseStatus` - 基础状态：ENABLE(1) 启用, DISABLE(0) 禁用
- `ReleaseStatus` - 发布状态：RELEASED(1) 已发布, NOT_RELEASED(0) 未发布

### 📄 租约相关
- `LeaseStatus` - 租约状态：SIGNING(1) 签约中, SIGNED(2) 已签约, CANCELED(3) 已取消, EXPIRED(4) 已到期, WITHDRAWING(5) 退租中, WITHDRAWN(6) 已退租, RENEWING(7) 续签中
- `LeaseSourceType` - 租约来源：NEW(1) 新签, RENEW(2) 续签

### 📅 预约相关
- `AppointmentStatus` - 预约状态：WAITING(1) 待看房, CANCELED(2) 已取消, VIEWED(3) 已看房

### 👤 用户相关
- `SystemUserType` - 系统用户类型：ADMIN(0) 管理员, COMMON(1) 普通用户

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📞 联系方式

- 项目维护者：[Your Name](mailto:your.email@example.com)
- 项目链接：[https://github.com/yourusername/lease-management-system](https://github.com/yourusername/lease-management-system)