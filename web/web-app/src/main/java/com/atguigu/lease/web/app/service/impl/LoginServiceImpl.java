package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.email.EmailService;
import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.common.utils.JwtUtil;
import com.atguigu.lease.common.utils.PasswordUtil;
import com.atguigu.lease.common.utils.VerifyCodeUtil;
import com.atguigu.lease.model.entity.UserInfo;
import com.atguigu.lease.model.enums.BaseStatus;
import com.atguigu.lease.web.app.service.LoginService;
import com.atguigu.lease.web.app.service.UserInfoService;
import com.atguigu.lease.web.app.vo.user.LoginVo;
import com.atguigu.lease.web.app.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LoginServiceImpl implements LoginService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserInfoService userInfoService;

    @Override
    public void getEmailCode(String email) {

        // 1. 检查邮箱是否为空
        if (!StringUtils.hasText(email)) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_EMAIL_EMPTY);
        }

        // 2. 检查邮箱格式是否正确
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (!email.matches(emailRegex)) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_EMAIL_FORMAT_ERROR);
        }

        // 3. 检查Redis中是否已经存在该邮箱的key
        String key = RedisConstant.APP_LOGIN_EMAIL_PREFIX + email;
        boolean hasKey = redisTemplate.hasKey(key);
        if (hasKey) {
            Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            if (RedisConstant.APP_LOGIN_EMAIL_CODE_TTL_SEC - expire < RedisConstant.APP_LOGIN_EMAIL_CODE_RESEND_TIME_SEC) {
                throw new LeaseException(ResultCodeEnum.APP_SEND_EMAIL_TOO_OFTEN);
            }
        }

        // 4. 发送验证码邮件，并将验证码存入Redis
        String verifyCode = VerifyCodeUtil.getVerifyCode(6);
        emailService.sendVerifyCode(email, verifyCode);
        redisTemplate.opsForValue().set(key, verifyCode, RedisConstant.APP_LOGIN_EMAIL_CODE_TTL_SEC, TimeUnit.SECONDS);
    }

    @Override
    public String login(LoginVo loginVo) {
        log.info("用户尝试验证码登录, email={}", loginVo.getEmail());

        // 1. 判断邮箱和验证码是否为空
        if (!StringUtils.hasText(loginVo.getEmail())) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_EMAIL_EMPTY);
        }

        if (!StringUtils.hasText(loginVo.getCode())) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_EMPTY);
        }

        // 2. 校验验证码
        String key = RedisConstant.APP_LOGIN_EMAIL_PREFIX + loginVo.getEmail();
        String code = redisTemplate.opsForValue().get(key);
        if (code == null) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_EXPIRED);
        }

        if (!code.equals(loginVo.getCode())) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_ERROR);
        }

        // 3. 判断用户是否存在，不存在则注册（创建用户）
        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getEmail, loginVo.getEmail());
        UserInfo userInfo = userInfoService.getOne(queryWrapper);
        if (userInfo == null) {
            userInfo = new UserInfo();
            userInfo.setEmail(loginVo.getEmail());
            userInfo.setStatus(BaseStatus.ENABLE);
            String nickname = "用户-" + loginVo.getEmail().split("@")[0];
            userInfo.setNickname(nickname);
            userInfoService.save(userInfo);
            log.info("新用户自动注册, email={}, nickname={}", loginVo.getEmail(), nickname);
        }

        // 4. 判断用户是否被禁
        if (userInfo.getStatus().equals(BaseStatus.DISABLE)) {
            throw new LeaseException(ResultCodeEnum.APP_ACCOUNT_DISABLED_ERROR);
        }

        // 5. 删除Redis中的验证码（防止重复使用）
        redisTemplate.delete(key);

        // 6. 创建并返回JWT Token
        log.info("用户验证码登录成功, userId={}, email={}", userInfo.getId(), loginVo.getEmail());
        return JwtUtil.createToken(userInfo.getId(), loginVo.getEmail());
    }

    @Override
    public String loginByPassword(LoginVo loginVo) {
        log.info("用户尝试密码登录, email={}", loginVo.getEmail());
        // 1. 校验邮箱和密码
        if (!StringUtils.hasText(loginVo.getEmail())) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_EMAIL_EMPTY);
        }
        if (!StringUtils.hasText(loginVo.getPassword())) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR, "密码不能为空");
        }

        // 2. 查找用户（password 字段 select=false，需要显式查询）
        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getEmail, loginVo.getEmail());
        queryWrapper.select(UserInfo::getId, UserInfo::getEmail, UserInfo::getPassword, UserInfo::getStatus);
        UserInfo userInfo = userInfoService.getOne(queryWrapper);
        if (userInfo == null) {
            throw new LeaseException(ResultCodeEnum.APP_ACCOUNT_NOT_EXIST_ERROR);
        }

        // 3. 判断用户是否被禁
        if (userInfo.getStatus().equals(BaseStatus.DISABLE)) {
            throw new LeaseException(ResultCodeEnum.APP_ACCOUNT_DISABLED_ERROR);
        }

        // 4. 校验密码
        if (!StringUtils.hasText(userInfo.getPassword())) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR, "该账号未设置密码，请使用验证码登录");
        }
        if (!PasswordUtil.matches(loginVo.getPassword(), userInfo.getPassword())) {
            throw new LeaseException(ResultCodeEnum.APP_ACCOUNT_ERROR);
        }

        // 5. 创建并返回JWT Token
        log.info("用户密码登录成功, userId={}, email={}", userInfo.getId(), loginVo.getEmail());
        return JwtUtil.createToken(userInfo.getId(), loginVo.getEmail());
    }

    @Override
    public UserInfoVo getUserInfoById(Long id) {
        UserInfo userInfo = userInfoService.getById(id);
        if (userInfo == null) {
            throw new LeaseException(ResultCodeEnum.APP_ACCOUNT_NOT_EXIST_ERROR);
        }
        return new UserInfoVo(userInfo.getNickname(), userInfo.getAvatarUrl(), userInfo.getEmail(), userInfo.getPhone());
    }
}