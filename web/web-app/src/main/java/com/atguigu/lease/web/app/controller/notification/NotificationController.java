package com.atguigu.lease.web.app.controller.notification;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.model.entity.Notification;
import com.atguigu.lease.common.login.LoginUser;
import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.web.app.service.NotificationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Tag(name = "通知消息")
@RestController
@RequestMapping("/app/notification")
@Slf4j
public class NotificationController {

    @Autowired
    private NotificationService service;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private Long getCurrentUserId() {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_AUTH);
        }
        return loginUser.getUserId();
    }

    /** 未读数 Redis 键（NotificationConsumer 消费成功时 INCR） */
    private String unreadKey(Long userId) {
        return RedisConstant.NOTIFICATION_UNREAD_PREFIX + userId;
    }

    @Operation(summary = "获取通知列表（最近50条）")
    @GetMapping("list")
    public Result<List<Notification>> list() {
        Long userId = getCurrentUserId();
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getUserId, userId);
        queryWrapper.orderByDesc(Notification::getCreateTime);
        queryWrapper.last("LIMIT 50");
        List<Notification> result = service.list(queryWrapper);
        return Result.ok(result);
    }

    @Operation(summary = "分页获取通知列表")
    @GetMapping("page")
    public Result<IPage<Notification>> page(@RequestParam long current, @RequestParam long size) {
        Long userId = getCurrentUserId();
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getUserId, userId);
        queryWrapper.orderByDesc(Notification::getCreateTime);
        Page<Notification> page = new Page<>(current, size);
        IPage<Notification> result = service.page(page, queryWrapper);
        return Result.ok(result);
    }

    @Operation(summary = "获取未读通知数量")
    @GetMapping("unreadCount")
    public Result<Long> unreadCount() {
        Long userId = getCurrentUserId();
        // 优先读 Redis（消费者 INCR 维护），键缺失时回源 DB 并回填
        String key = unreadKey(userId);
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            return Result.ok(Long.parseLong(cached));
        }
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getUserId, userId);
        queryWrapper.eq(Notification::getIsRead, 0);
        long count = service.count(queryWrapper);
        stringRedisTemplate.opsForValue().set(key, String.valueOf(count));
        return Result.ok(count);
    }

    @Operation(summary = "标记通知为已读")
    @PostMapping("markAsRead")
    public Result markAsRead(@RequestParam Long id) {
        if (id == null || id <= 0) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR, "通知ID不能为空");
        }
        Long userId = getCurrentUserId();
        LambdaUpdateWrapper<Notification> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Notification::getId, id);
        updateWrapper.eq(Notification::getUserId, userId);
        updateWrapper.eq(Notification::getIsRead, 0);
        updateWrapper.set(Notification::getIsRead, 1);
        boolean updated = service.update(updateWrapper);
        if (updated) {
            // 同步扣减 Redis 未读数；键缺失（Redis 重启/清空）时 DECR 会得负数，
            // 不能简单置 0——需以本次更新后的 DB 未读数重建，否则角标为 0 但仍有未读
            Long remaining = stringRedisTemplate.opsForValue().decrement(unreadKey(userId));
            if (remaining != null && remaining < 0) {
                LambdaQueryWrapper<Notification> recalc = new LambdaQueryWrapper<>();
                recalc.eq(Notification::getUserId, userId);
                recalc.eq(Notification::getIsRead, 0);
                stringRedisTemplate.opsForValue().set(unreadKey(userId), String.valueOf(service.count(recalc)));
            }
        }
        return Result.ok();
    }

    @Operation(summary = "标记所有通知为已读")
    @PostMapping("markAllAsRead")
    public Result markAllAsRead() {
        Long userId = getCurrentUserId();
        LambdaUpdateWrapper<Notification> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Notification::getUserId, userId);
        updateWrapper.eq(Notification::getIsRead, 0);
        updateWrapper.set(Notification::getIsRead, 1);
        service.update(updateWrapper);
        // 全部已读：直接删除 Redis 计数键（下次查询回源 DB 得 0）
        stringRedisTemplate.delete(unreadKey(userId));
        return Result.ok();
    }
}
