package com.atguigu.lease.web.app.controller.post;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.mq.RabbitMQConfiguration;
import com.atguigu.lease.common.mq.dto.PostLikeMessage;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.web.app.custom.LoginUser;
import com.atguigu.lease.web.app.custom.holder.LoginUserHolder;
import com.atguigu.lease.web.app.mapper.PostInfoMapper;
import com.atguigu.lease.model.entity.PostInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "帖子点赞")
@RestController
@RequestMapping("/app/post/like")
@Slf4j
public class PostLikeController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private PostInfoMapper postInfoMapper;

    /**
     * Lua脚本：原子操作点赞/取消点赞
     */
    private static final String TOGGLE_LIKE_SCRIPT =
            "local usersKey = KEYS[1] " +
            "local countKey = KEYS[2] " +
            "local userId = ARGV[1] " +
            "local isMember = redis.call('SISMEMBER', usersKey, userId) " +
            "if isMember == 1 then " +
            "  redis.call('SREM', usersKey, userId) " +
            "  redis.call('DECR', countKey) " +
            "  return {0, redis.call('GET', countKey)} " +
            "else " +
            "  redis.call('SADD', usersKey, userId) " +
            "  redis.call('INCR', countKey) " +
            "  return {1, redis.call('GET', countKey)} " +
            "end";

    @Operation(summary = "点赞/取消点赞")
    @PostMapping("toggle/{postId}")
    public Result<Map<String, Object>> toggle(@PathVariable Long postId) {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_AUTH);
        }
        // 限流: 1秒内最夞点赞10次
        String rateKey = RedisConstant.RATE_LIMIT_PREFIX + loginUser.getUserId() + ":like";
        String rateVal = stringRedisTemplate.opsForValue().get(rateKey);
        if (rateVal != null && Integer.parseInt(rateVal) >= 10) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR, "操作太频繁");
        }

        String usersKey = RedisConstant.POST_LIKE_USERS_PREFIX + postId + RedisConstant.POST_LIKE_USERS_SUFFIX;
        String countKey = RedisConstant.POST_LIKE_USERS_PREFIX + postId + RedisConstant.POST_LIKE_COUNT_SUFFIX;

        // 若Redis中无count缓存，从数据库初始化
        if (Boolean.FALSE.equals(stringRedisTemplate.hasKey(countKey))) {
            PostInfo postInfo = postInfoMapper.selectById(postId);
            int dbCount = postInfo != null && postInfo.getLikeCount() != null ? postInfo.getLikeCount() : 0;
            stringRedisTemplate.opsForValue().set(countKey, String.valueOf(dbCount));
        }

        DefaultRedisScript<List> script = new DefaultRedisScript<>(TOGGLE_LIKE_SCRIPT, List.class);
        List<?> result = stringRedisTemplate.execute(
                script, Arrays.asList(usersKey, countKey), String.valueOf(loginUser.getUserId()));

        boolean isLike = Long.parseLong(result.get(0).toString()) == 1;
        long newCount = Long.parseLong(result.get(1).toString());

        // 发MQ异步落库
        PostLikeMessage msg = new PostLikeMessage(postId, loginUser.getUserId(), isLike ? "like" : "unlike");
        rabbitTemplate.convertAndSend(
                RabbitMQConfiguration.POST_LIKE_EXCHANGE,
                RabbitMQConfiguration.POST_LIKE_ROUTING_KEY,
                msg);

        // 更新限流计数
        Long rateCount = stringRedisTemplate.opsForValue().increment(rateKey);
        if (rateCount != null && rateCount == 1) {
            stringRedisTemplate.expire(rateKey, 1, java.util.concurrent.TimeUnit.SECONDS);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("isLiked", isLike);
        response.put("likeCount", newCount);
        return Result.ok(response);
    }

    @Operation(summary = "获取帖子点赞数")
    @GetMapping("count/{postId}")
    public Result<Long> likeCount(@PathVariable Long postId) {
        String countKey = RedisConstant.POST_LIKE_USERS_PREFIX + postId + RedisConstant.POST_LIKE_COUNT_SUFFIX;
        String count = stringRedisTemplate.opsForValue().get(countKey);
        return Result.ok(count != null ? Long.parseLong(count) : 0L);
    }

    @Operation(summary = "检查当前用户是否点赞")
    @GetMapping("isLiked/{postId}")
    public Result<Boolean> isLiked(@PathVariable Long postId) {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            return Result.ok(false);
        }
        String usersKey = RedisConstant.POST_LIKE_USERS_PREFIX + postId + RedisConstant.POST_LIKE_USERS_SUFFIX;
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(usersKey, String.valueOf(loginUser.getUserId()));
        return Result.ok(Boolean.TRUE.equals(isMember));
    }
}
