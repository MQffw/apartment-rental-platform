# 7.4.2 登录管理（邮箱验证版）实现文档

## 概述
本文档详细描述了在web-app模块中实现邮箱验证码登录功能的完整过程，包括所有新增和修改的文件、配置项以及注意事项。

## 实现内容

### 1. 错误码枚举更新
**文件**: `common/src/main/java/com/atguigu/lease/common/result/ResultCodeEnum.java`

**修改内容**:
- 添加了三个新的错误码：
  - `APP_LOGIN_EMAIL_EMPTY(508, "邮箱不能为空")`
  - `APP_LOGIN_EMAIL_FORMAT_ERROR(509, "邮箱格式不正确")`
  - `APP_SEND_EMAIL_TOO_OFTEN(510, "发送过于频繁，请稍后再试")`

### 2. 邮件配置类
**文件**: `common/src/main/java/com/atguigu/lease/common/email/EmailProperties.java`

**功能**: 配置验证码相关参数
- `length`: 验证码长度（默认6位）
- `ttl`: 验证码有效期（默认300秒/5分钟）
- `resend`: 重发间隔（默认60秒）

### 3. 邮件服务接口
**文件**: `common/src/main/java/com/atguigu/lease/common/email/EmailService.java`

**功能**: 定义邮件发送接口
- `sendVerifyCode(String toEmail, String code)`: 发送验证码到指定邮箱

### 4. 邮件服务实现
**文件**: `common/src/main/java/com/atguigu/lease/common/email/EmailServiceImpl.java`

**功能**: 实现邮件发送
- 使用Spring Boot Mail的`JavaMailSender`
- 发送包含验证码的邮件
- 异常处理：邮件发送失败时抛出运行时异常

### 5. 验证码生成工具
**文件**: `common/src/main/java/com/atguigu/lease/common/utils/VerifyCodeUtil.java`

**功能**: 生成指定位数的数字验证码
- `getVerifyCode(int length)`: 生成指定长度的数字验证码

### 6. Redis常量更新
**文件**: `common/src/main/java/com/atguigu/lease/common/constant/RedisConstant.java`

**新增常量**:
- `APP_LOGIN_EMAIL_PREFIX`: 邮箱验证码Redis前缀（"app:login:email:"）
- `APP_LOGIN_EMAIL_CODE_TTL_SEC`: 验证码有效期（300秒）
- `APP_LOGIN_EMAIL_CODE_RESEND_TIME_SEC`: 重发间隔（60秒）

### 7. 登录对象更新
**文件**: `web-app/src/main/java/com/atguigu/lease/web/app/vo/user/LoginVo.java`

**修改内容**:
- 将`phone`字段改为`email`字段
- 更新Swagger描述为"登录对象"
- 字段说明：邮箱、验证码

### 8. 登录用户对象
**文件**: `web-app/src/main/java/com/atguigu/lease/web/app/custom/LoginUser.java`

**功能**: 存储当前登录用户信息
- `userId`: 用户ID
- `username`: 用户名（邮箱）

### 9. 登录用户持有者
**文件**: `web-app/src/main/java/com/atguigu/lease/web/app/custom/holder/LoginUserHolder.java`

**功能**: 基于ThreadLocal管理当前登录用户
- `setLoginUser()`: 设置当前用户
- `getLoginUser()`: 获取当前用户
- `clear()`: 清理用户信息

### 10. 认证拦截器
**文件**: `web-app/src/main/java/com/atguigu/lease/web/app/custom/interceptor/AuthenticationInterceptor.java`

**功能**: 验证JWT Token
- 从请求头获取`access-token`
- 解析JWT Token获取用户信息
- 将用户信息存储到`LoginUserHolder`
- 请求完成后清理用户信息

### 11. Web配置类
**文件**: `web-app/src/main/java/com/atguigu/lease/web/app/custom/config/WebMvcConfiguration.java`

**功能**:
- 注册认证拦截器
- 拦截所有`/app/**`路径，排除`/app/login/**`
- 配置Knife4j全局安全方案，支持在Header中传递`access-token`

### 12. 登录服务接口
**文件**: `web-app/src/main/java/com/atguigu/lease/web/app/service/LoginService.java`

**方法**:
- `getEmailCode(String email)`: 获取邮箱验证码
- `login(LoginVo loginVo)`: 登录/注册
- `getUserInfoById(Long id)`: 获取用户信息

### 13. 登录服务实现
**文件**: `web-app/src/main/java/com/atguigu/lease/web/app/service/impl/LoginServiceImpl.java`

#### getEmailCode方法逻辑:
1. 检查邮箱是否为空
2. 验证邮箱格式
3. 检查重发间隔（60秒内不能重复发送）
4. 生成验证码并发送邮件
5. 将验证码存入Redis（有效期5分钟）

#### login方法逻辑:
1. 验证邮箱和验证码是否为空
2. 校验验证码（从Redis获取并比对）
3. 查询用户是否存在，不存在则创建新用户
4. 检查用户状态（是否被禁用）
5. 删除Redis中的验证码
6. 生成并返回JWT Token

### 14. 控制器更新
**文件**: `web-app/src/main/java/com/atguigu/lease/web/app/controller/login/LoginController.java`

**更新内容**:
- 注入`LoginService`
- `getCode`: 调用`getEmailCode`方法，参数改为email
- `login`: 调用`login`方法并返回Token
- `info`: 调用`getUserInfoById`方法获取用户信息

## 需要手动添加的配置

### 1. 邮件配置
在`application.yml`中添加以下配置：

```yaml
spring:
  mail:
    host: smtp.qq.com  # SMTP服务器
    port: 587           # 端口
    username: your-email@qq.com  # 邮箱地址
    password: your-authorization-code  # 授权码，不是登录密码
    protocol: smtp
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true

verify:
  code:
    length: 6        # 验证码长度
    ttl: 300         # 有效期（秒）
    resend: 60       # 重发间隔（秒）
```

### 2. Redis配置
```yaml
spring:
  data:
    redis:
      host: 192.168.10.101  # Redis服务器地址
      port: 6379             # Redis端口
      database: 0            # 数据库索引
```

### 3. 依赖配置
在`common`模块的`pom.xml`中添加邮件依赖：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

## 数据库表结构

```sql
-- 用户表
CREATE TABLE `user_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `email` varchar(100) NOT NULL COMMENT '邮箱（登录账号）',
  `nickname` varchar(50) DEFAULT NULL COMMENT '用户昵称',
  `avatar_url` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `status` tinyint DEFAULT '1' COMMENT '账号状态（1：正常，0：禁用）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';
```

## 使用说明

### 1. 获取验证码
- **接口**: GET `/app/login/getCode`
- **参数**: email (邮箱地址)
- **响应**: 成功或错误信息

### 2. 登录/注册
- **接口**: POST `/app/login`
- **参数**:
  ```json
  {
    "email": "user@example.com",
    "code": "123456"
  }
  ```
- **响应**: JWT Token

### 3. 获取用户信息
- **接口**: GET `/app/info`
- **Header**: access-token (JWT Token)
- **响应**: 用户信息

## 注意事项

1. **邮箱配置**: 需要在邮箱服务商处开启SMTP服务并获取授权码
2. **Redis连接**: 确保Redis服务器可访问且配置正确
3. **拦截器**: 所有受保护的接口都需要在Header中传递`access-token`
4. **错误码**: 新增的错误码已添加到ResultCodeEnum中
5. **Knife4j**: 已配置全局安全方案，可直接在Knife4j界面调试受保护接口
6. **验证码安全**: 验证码有效期5分钟，60秒内不能重复发送
7. **用户注册**: 首次使用邮箱登录会自动创建用户账户

## 实现状态
✅ 所有功能已完整实现，包括：
- 邮箱验证码发送
- 登录注册逻辑
- JWT Token生成与验证
- 用户信息查询
- 认证拦截器
- Knife4j安全配置