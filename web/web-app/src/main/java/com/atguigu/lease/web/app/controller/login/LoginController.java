package com.atguigu.lease.web.app.controller.login;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.common.utils.PasswordUtil;
import com.atguigu.lease.model.entity.UserInfo;
import com.atguigu.lease.web.app.custom.LoginUser;
import com.atguigu.lease.web.app.custom.holder.LoginUserHolder;
import com.atguigu.lease.web.app.service.LoginService;
import com.atguigu.lease.web.app.service.UserInfoService;
import com.atguigu.lease.web.app.vo.user.LoginVo;
import com.atguigu.lease.web.app.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "登录管理")
@RestController
@RequestMapping("/app/")
@Slf4j
public class LoginController {

    @Autowired
    private LoginService service;

    @Autowired
    private UserInfoService userInfoService;

    @GetMapping("login/getCode")
    @Operation(summary = "获取邮箱验证码")
    public Result getCode(@RequestParam String email) {
        service.getEmailCode(email);
        return Result.ok();
    }

    @PostMapping("login")
    @Operation(summary = "验证码登录")
    public Result<String> login(@Valid @RequestBody LoginVo loginVo) {
        String token = service.login(loginVo);
        return Result.ok(token);
    }

    @PostMapping("loginByPassword")
    @Operation(summary = "密码登录")
    public Result<String> loginByPassword(@Valid @RequestBody LoginVo loginVo) {
        String token = service.loginByPassword(loginVo);
        return Result.ok(token);
    }

    @PostMapping("setPassword")
    @Operation(summary = "设置密码")
    public Result setPassword(@RequestParam String password) {
        if (!StringUtils.hasText(password) || password.length() < 6) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR, "密码长度不能少于6位");
        }
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_AUTH);
        }
        UserInfo userInfo = userInfoService.getById(loginUser.getUserId());
        if (userInfo == null) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR, "用户不存在");
        }
        userInfo.setPassword(PasswordUtil.encode(password));
        userInfoService.updateById(userInfo);
        return Result.ok();
    }

    @GetMapping("info")
    @Operation(summary = "获取登录用户信息")
    public Result<UserInfoVo> info() {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_AUTH);
        }
        UserInfoVo info = service.getUserInfoById(loginUser.getUserId());
        return Result.ok(info);
    }

    @GetMapping("hasPassword")
    @Operation(summary = "检查用户是否已设置密码")
    public Result<Boolean> hasPassword() {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_AUTH);
        }
        // password 字段 select=false，需要显式查询
        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getId, loginUser.getUserId());
        queryWrapper.select(UserInfo::getPassword);
        UserInfo userInfo = userInfoService.getOne(queryWrapper);
        return Result.ok(userInfo != null && StringUtils.hasText(userInfo.getPassword()));
    }
}
