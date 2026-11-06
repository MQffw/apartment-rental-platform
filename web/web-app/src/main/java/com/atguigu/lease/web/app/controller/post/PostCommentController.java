package com.atguigu.lease.web.app.controller.post;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.mq.RabbitMQConfiguration;
import com.atguigu.lease.common.mq.dto.PostCommentMessage;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.model.entity.PostComment;
import com.atguigu.lease.model.entity.UserInfo;
import com.atguigu.lease.common.login.LoginUser;
import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.common.ratelimit.RateLimit;
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
    @RateLimit(max = 3, window = 10, message = "评论太频繁，请稍后再试")
    @PostMapping("create")
    public Result<Long> create(@RequestBody PostCommentMessage msg) {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_AUTH);
        }
        // Redis 序列预生成 commentId（作为消费端幂等键），发 MQ 异步入库。
        // 序列缺失时先以 DB 最大 id 对齐，避免与 AUTO_INCREMENT 主键冲突（同发帖逻辑）
        String seqKey = "post:comment:id:seq";
        if (Boolean.FALSE.equals(stringRedisTemplate.hasKey(seqKey))) {
            PostComment max = commentService.getOne(new LambdaQueryWrapper<PostComment>()
                    .select(PostComment::getId)
                    .orderByDesc(PostComment::getId)
                    .last("LIMIT 1"));
            stringRedisTemplate.opsForValue().setIfAbsent(seqKey,
                    String.valueOf(max != null ? max.getId() : 0L));
        }
        Long commentId = stringRedisTemplate.opsForValue().increment(seqKey);
        msg.setCommentId(commentId);
        msg.setUserId(loginUser.getUserId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfiguration.POST_COMMENT_EXCHANGE,
                RabbitMQConfiguration.POST_COMMENT_ROUTING_KEY,
                msg);
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
