package com.atguigu.lease.web.admin.controller.notification;

import com.atguigu.lease.common.mq.RabbitMQConfiguration;
import com.atguigu.lease.common.mq.dto.NotificationMessage;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.model.entity.UserInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "公告发送")
@RestController
@RequestMapping("/admin/announcement")
@Slf4j
public class AnnouncementController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private com.atguigu.lease.web.admin.mapper.UserInfoMapper userInfoMapper;

    @Operation(summary = "发送系统公告")
    @PostMapping("send")
    public Result send(@RequestBody Map<String, Object> params) {
        String title = (String) params.get("title");
        String content = (String) params.get("content");
        String target = (String) params.getOrDefault("target", "all");

        if (title == null || title.isEmpty()) {
            return Result.fail(ResultCodeEnum.PARAM_ERROR.getCode(), "标题不能为空");
        }

        if (content == null || content.isEmpty()) {
            return Result.fail(ResultCodeEnum.PARAM_ERROR.getCode(), "内容不能为空");
        }

        if ("all".equals(target)) {
            // 查询所有用户并批量发送
            List<UserInfo> users = userInfoMapper.selectList(
                new LambdaQueryWrapper<UserInfo>().select(UserInfo::getId));
            int sent = 0;
            for (UserInfo user : users) {
                rabbitTemplate.convertAndSend(
                        RabbitMQConfiguration.NOTIFICATION_EXCHANGE,
                        RabbitMQConfiguration.NOTIFICATION_ROUTING_KEY,
                        new NotificationMessage(user.getId(), title, content, 4, null));
                sent++;
            }
            log.info("系统公告发送完成: 发送给 {} 个用户", sent);
            return Result.ok("已发送给" + sent + "个用户");
        } else {
            // 指定用户ID
            if (target == null || target.trim().isEmpty()) {
                return Result.fail(ResultCodeEnum.PARAM_ERROR.getCode(), "请输入用户ID");
            }
            Long userId;
            try {
                userId = Long.parseLong(target.trim());
            } catch (NumberFormatException e) {
                return Result.fail(ResultCodeEnum.PARAM_ERROR.getCode(), "用户ID格式错误");
            }
            rabbitTemplate.convertAndSend(
                    RabbitMQConfiguration.NOTIFICATION_EXCHANGE,
                    RabbitMQConfiguration.NOTIFICATION_ROUTING_KEY,
                    new NotificationMessage(userId, title, content, 4, null));
            return Result.ok("已发送给用户 " + userId);
        }
    }
}
