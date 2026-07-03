package com.atguigu.lease.web.app.controller.post;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.mq.RabbitMQConfiguration;
import com.atguigu.lease.common.mq.dto.PostCommentMessage;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.model.entity.PostComment;
import com.atguigu.lease.model.entity.UserInfo;
import com.atguigu.lease.web.app.custom.LoginUser;
import com.atguigu.lease.web.app.custom.holder.LoginUserHolder;
import com.atguigu.lease.web.app.mapper.UserInfoMapper;
import com.atguigu.lease.web.app.service.PostCommentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Tag(name = "帖子评论")
@RestController
@RequestMapping("/app/post/comment")
@Slf4j
public class PostCommentController {

    @Autowired
    private PostCommentService commentService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Operation(summary = "发布评论（异步入库）")
    @PostMapping("create")
    public Result<Long> create(@RequestBody PostCommentMessage msg) {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_AUTH);
        }
        // 限流: 10秒内最多3次
        String rateKey = RedisConstant.RATE_LIMIT_PREFIX + loginUser.getUserId() + ":comment";
        String rateVal = stringRedisTemplate.opsForValue().get(rateKey);
        if (rateVal != null && Integer.parseInt(rateVal) >= 3) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR, "评论太频繁，请稍后再试");
        }
        msg.setUserId(loginUser.getUserId());
        Long commentId = stringRedisTemplate.opsForValue().increment("post:comment:id:seq");
        msg.setCommentId(commentId);
        rabbitTemplate.convertAndSend(
                RabbitMQConfiguration.POST_COMMENT_EXCHANGE,
                RabbitMQConfiguration.POST_COMMENT_ROUTING_KEY,
                msg);
        // 设置限流
        Long rateCount = stringRedisTemplate.opsForValue().increment(rateKey);
        if (rateCount != null && rateCount == 1) {
            stringRedisTemplate.expire(rateKey, 10, TimeUnit.SECONDS);
        }
        return Result.ok(commentId);
    }

    @Operation(summary = "获取帖子评论列表")
    @GetMapping("list/{postId}")
    public Result<List<Map<String, Object>>> list(@PathVariable Long postId) {
        LambdaQueryWrapper<PostComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostComment::getPostId, postId);
        wrapper.eq(PostComment::getStatus, 1);
        wrapper.orderByAsc(PostComment::getCreateTime);
        List<PostComment> comments = commentService.list(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (PostComment c : comments) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("content", c.getContent());
            map.put("parentId", c.getParentId());
            map.put("replyToId", c.getReplyToId());
            map.put("likeCount", c.getLikeCount());
            map.put("createTime", c.getCreateTime());
            UserInfo user = userInfoMapper.selectById(c.getUserId());
            if (user != null) {
                map.put("nickname", user.getNickname());
                map.put("avatarUrl", user.getAvatarUrl());
            }
            if (c.getReplyToId() != null && c.getReplyToId() > 0) {
                UserInfo replyUser = userInfoMapper.selectById(c.getReplyToId());
                if (replyUser != null) {
                    map.put("replyToNickname", replyUser.getNickname());
                }
            }
            result.add(map);
        }
        return Result.ok(result);
    }
}
