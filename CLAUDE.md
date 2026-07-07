# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

房屋租赁管理系统（Lease Management System），全栈项目，包含：
- **后端**：Spring Boot 3.0.5 Maven 多模块项目（本仓库）
- **管理端前端**：Vue 3 + Element Plus（`C:\Users\mq\Desktop\rentHouseAdmin`）
- **用户端前端**：Vue 3 + Vant 4（`C:\Users\mq\Desktop\rentHouseH5`）

后端提供管理员后台（web-admin, 端口 8080）和用户移动端（web-app, 端口 8081）双端 API。

---

## 后端

### 构建与运行

```bash
# 根目录编译打包（必须先执行）
mvn clean install

# 启动后台管理端（端口 8080）
cd web/web-admin
mvn spring-boot:run

# 启动用户移动端（端口 8081）
cd web/web-app
mvn spring-boot:run
```

**环境要求**：JDK 17+、MySQL 8.x、Redis 7.x、MinIO

**API 文档**：启动后访问 http://localhost:8080/doc.html（管理端）或 http://localhost:8081/doc.html（用户端）

### 模块依赖链

```
model ← common ← web-admin
                ← web-app
```

- **model** — 实体类（`entity/`）和枚举（`enums/`），所有实体继承 `BaseEntity`（含 id、createTime、updateTime、isDeleted）
- **common** — 公共组件：`Result<T>` 统一返回、`GlobalExceptionHandler`、`JwtUtil`、Redis/MinIO/Email 配置、`LoginUserHolder`（ThreadLocal）
- **web-admin** — 管理员后台，16 个控制器，路径前缀 `/admin/**`
- **web-app** — 用户移动端，9 个控制器，路径前缀 `/app/**`

### 后端架构模式

- **统一返回**：所有控制器返回 `Result<T>`（code/message/data），错误码定义在 `ResultCodeEnum`
- **认证**：JWT Token 通过 `access-token` 请求头传递。管理端用图形验证码+密码登录；用户端用邮箱验证码登录
- **拦截器**：`AuthenticationInterceptor` 在两个 web 模块中分别拦截受保护路径，登录上下文通过 `LoginUserHolder`（ThreadLocal）传递
- **MyBatis-Plus**：实体用 `@TableName`/`@TableField` 注解，Mapper XML 文件在各模块的 `src/main/resources/mapper/` 下，`MybatisMetaObjectHandler` 自动填充时间戳
- **逻辑删除**：`isDeleted` 字段，配合 MyBatis-Plus 全局配置
- **定时任务**：web-admin 中 `ScheduledTasks` 每日零点自动检查并更新到期租约状态
- **MinIO 条件加载**：仅当配置了 `minio.endpoint` 属性时才创建 MinIO Bean

### 认证流程

**管理端 (web-admin)**：
1. `GET /admin/login/captcha` 获取图形验证码
2. `POST /admin/login` 用户名+密码登录，返回 JWT
3. 后续请求 Header 携带 `access-token: <JWT>`

**用户端 (web-app)**：
1. `GET /app/login/getCode?email=xxx` 获取邮箱验证码
2. `POST /app/login` 邮箱+验证码登录（新用户自动注册），返回 JWT
3. 后续请求 Header 携带 `access-token: <JWT>`

### 基础设施依赖

配置在各模块的 `application.yml` 中，默认连接 `192.168.10.101`：
- MySQL 3306（数据库 `lease`）
- Redis 6379
- MinIO 9000（桶 `lease`）
- SMTP 邮件服务（用户端邮箱验证码）

### 后端代码规范

- 基础包名：`com.atguigu.lease`
- 实体类使用 Lombok `@Data` + Swagger `@Schema` 注解
- 枚举实现 `BaseEnum` 接口，支持 MyBatis-Plus 自动转换
- 控制器按领域分包：`login/`、`apartment/`、`lease/`、`system/`、`user/`（管理端）；`login/`、`apartment/`、`room/`、`appointment/`、`agreement/`、`history/`（用户端）
- VO（视图对象）按领域分包在各模块的 `vo/` 目录下

### 核心枚举速查

| 枚举 | 含义 | 关键值 |
|---|---|---|
| `ItemType` | 关联类型 | APARTMENT(1), ROOM(2) |
| `BaseStatus` | 基础状态 | ENABLE(1), DISABLE(0) |
| `ReleaseStatus` | 发布状态 | RELEASED(1), NOT_RELEASED(0) |
| `LeaseStatus` | 租约状态 | SIGNING(1), SIGNED(2), CANCELED(3), EXPIRED(4), WITHDRAWING(5), WITHDRAWN(6), RENEWING(7) |
| `AppointmentStatus` | 预约状态 | WAITING(1), CANCELED(2), VIEWED(3) |
| `SystemUserType` | 系统用户类型 | ADMIN(0), COMMON(1) |

---

## 管理端前端 (rentHouseAdmin)

**路径**：`C:\Users\mq\Desktop\rentHouseAdmin`
**技术栈**：Vue 3 + TypeScript + Vite 4 + Element Plus + Pinia
**后端连接**：开发环境代理 `/admin` 请求到 `http://localhost:8080`

### 常用命令

```bash
npm install          # 安装依赖
npm run dev          # 启动开发服务器
npm run build        # 生产构建
npm run lint         # ESLint 检查
npm run lint:eslint  # ESLint 检查并自动修复
npm run lint:style   # Stylelint 检查
npm run format       # Prettier 格式化
```

### 架构要点

- **状态管理**：Pinia，三个 store 模块 — `user`（token + 用户信息，持久化）、`settings`（主题/侧边栏）、`tabsBar`（标签页导航）
- **路由**：Hash 模式，静态路由定义在 `src/router/constantRoutes.ts`，导航守卫校验 token
- **HTTP 客户端**：Axios 封装在 `src/utils/http/`，请求拦截器自动附加 `access-token`，响应拦截器处理过期码（305/601/602）
- **API 组织**：按领域分目录在 `src/api/` 下，每个目录含 `index.ts`（接口函数）和 `types.ts`（类型定义）
- **ProTable 组件**：核心表格组件，封装了搜索、分页、列配置、数据请求，通过 `requestApi` prop 驱动
- **主题系统**：支持暗黑模式和动态主色调，通过 CSS 自定义属性实现
- **代码规范**：ESLint + Prettier + Stylelint + Husky + commitlint

### 目录结构

```
src/
  api/            # 按领域组织的 API 接口（apartmentManagement, rentManagement, system, user, upload）
  components/     # 全局组件（ProTable, SearchForm, SvgIcon, uploadImg 等）
  enums/          # 业务枚举和常量映射（constEnums.ts, httpEnums.ts）
  hooks/          # 组合式函数（useTheme, useMap, usePagination）
  layouts/        # 布局壳（SideBar, NavBar, TabsBar, Main）
  router/         # 路由配置（静态路由）
  store/          # Pinia 状态（user, settings, tabsBar）
  utils/          # 工具（HTTP 客户端、缓存、事件总线）
  views/          # 页面组件（home, login, apartmentManagement, rentManagement, system, userManagement）
```

---

## 用户端前端 (rentHouseH5)

**路径**：`C:\Users\mq\Desktop\rentHouseH5`
**技术栈**：Vue 3 + TypeScript + Vite 4 + Vant 4 + Pinia + Tailwind CSS
**后端连接**：开发环境代理 `/app` 请求到 `http://localhost:8081`
**应用名称**：硅谷租房

### 常用命令

```bash
npm install          # 安装依赖
npm run dev          # 启动开发服务器
npm run build        # 生产构建
npm run type-check   # TypeScript 类型检查
npm run lint         # ESLint 检查并自动修复
```

### 架构要点

- **移动端适配**：px-to-viewport PostCSS 插件（375px 设计稿基准），Vant 4 移动端组件库
- **状态管理**：Pinia，三个 store — `user`（token + 用户信息，持久化）、`cachedView`（keep-alive 缓存管理）、`darkMode`（暗黑模式）
- **路由**：Hash 模式，底部 TabBar 5 个主 tab（找房、圈子、我的房间、消息、个人中心）+ 8 个子页面
- **HTTP 客户端**：Axios 封装在 `src/utils/http/`，请求拦截器附加 `access-token`，响应拦截器处理过期码（305/501/601/602）
- **API 组织**：`src/api/search/`（房源/预约/租约等核心业务）、`src/api/user/`（登录认证）
- **无限滚动**：`PullDownRefreshContainer` 组件基于 IntersectionObserver 实现下拉刷新 + 上拉加载
- **暗黑模式**：CSS 自定义属性 + Vant ConfigProvider 主题切换 + 系统偏好检测
- **keep-alive 缓存**：组件 `name` 需与路由 `name` 一致（通过 `vite-plugin-vue-setup-extend` 支持 `<script setup>` 中设置 name），`meta.noCache: true` 的页面不缓存
- **代码规范**：ESLint + Prettier + Husky + commitlint

### 目录结构

```
src/
  api/            # API 接口（search/ 核心业务, user/ 认证）
  components/     # 自定义组件（RoomCard, ApartmentCard, SearchBar, PullDownRefreshContainer, NavBar, Tabbar）
  enums/          # 业务枚举和请求码常量
  hooks/          # 组合式函数（useIntersectionObserver, useMap, useToggleDarkMode, useToLoginPage）
  icons/svg/      # 60+ SVG 图标（家具、设施、生活用品等）
  layout/         # 主布局（NavBar + keep-alive router-view + Tabbar）
  router/         # 路由配置（tabBarRoutes + otherRoutes）
  store/          # Pinia 状态（user, cachedView, darkMode）
  styles/         # 全局样式（Less + Tailwind，暗黑模式变量）
  utils/          # 工具（HTTP 客户端、token 管理、类型检查）
  views/          # 页面组件（search, roomDetail, apartmentDetail, appointment, agreement, login 等）
```

---

## 全栈通信

前后端通过 RESTful API 通信，所有请求 Header 携带 `access-token: <JWT>`：

| 前端 | 后端模块 | API 前缀 | 认证方式 |
|---|---|---|---|
| rentHouseAdmin | web-admin | `/admin/` | 图形验证码 + 用户名密码 |
| rentHouseH5 | web-app | `/app/` | 邮箱验证码 |

**响应格式**统一为 `{ code: number, message: string, data: T }`，前端响应拦截器根据 `code` 判断成功/失败/登录过期。
