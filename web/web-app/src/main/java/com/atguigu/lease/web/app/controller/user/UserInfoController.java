package com.atguigu.lease.web.app.controller.user;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.model.entity.UserInfo;
import com.atguigu.lease.web.app.custom.LoginUser;
import com.atguigu.lease.web.app.custom.holder.LoginUserHolder;
import com.atguigu.lease.web.app.service.UserInfoService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "用户资料管理")
@RestController
@RequestMapping("/app/user")
@Slf4j
public class UserInfoController {

    @Autowired
    private UserInfoService service;

    private Long getCurrentUserId() {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_AUTH);
        }
        return loginUser.getUserId();
    }

    @Operation(summary = "获取当前用户资料")
    @GetMapping("info")
    public Result<UserInfo> getUserInfo() {
        Long userId = getCurrentUserId();
        UserInfo userInfo = service.getById(userId);
        if (userInfo == null) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR, "用户不存在");
        }
        return Result.ok(userInfo);
    }

    @Operation(summary = "更新用户资料")
    @PostMapping("updateProfile")
    public Result updateProfile(@RequestBody UserInfo updateInfo) {
        Long userId = getCurrentUserId();
        LambdaUpdateWrapper<UserInfo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserInfo::getId, userId);
        // 只允许更新昵称、头像、手机号
        if (updateInfo.getNickname() != null) {
            updateWrapper.set(UserInfo::getNickname, updateInfo.getNickname());
        }
        if (updateInfo.getAvatarUrl() != null) {
            updateWrapper.set(UserInfo::getAvatarUrl, updateInfo.getAvatarUrl());
        }
        if (updateInfo.getPhone() != null) {
            updateWrapper.set(UserInfo::getPhone, updateInfo.getPhone());
        }
        service.update(updateWrapper);
        return Result.ok();
    }
}
