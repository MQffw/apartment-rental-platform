package com.atguigu.lease.web.app.controller.post;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.mq.RabbitMQConfiguration;
import com.atguigu.lease.common.mq.dto.PostCreateMessage;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.model.entity.PostInfo;
import com.atguigu.lease.model.entity.UserInfo;
import com.atguigu.lease.model.enums.PostStatus;
import com.atguigu.lease.web.app.custom.LoginUser;
import com.atguigu.lease.web.app.custom.holder.LoginUserHolder;
import com.atguigu.lease.web.app.mapper.UserInfoMapper;
import com.atguigu.lease.web.app.service.PostService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Tag(name = "帖子/点评")
@RestController
@RequestMapping("/app/post")
@Slf4j
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserInfoMapper userInfoMapper;

    private LoginUser getCurrentUser() {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_AUTH);
        }
        return loginUser;
    }

    @Operation(summary = "发布帖子（异步入库）")
    @PostMapping("create")
    public Result<Long> create(@RequestBody PostCreateMessage msg) {
        LoginUser loginUser = getCurrentUser();
        // 限流检查：60秒内最多发1篇
        String rateKey = RedisConstant.RATE_LIMIT_PREFIX + loginUser.getUserId() + ":post";
        String rateVal = stringRedisTemplate.opsForValue().get(rateKey);
        if (rateVal != null && Integer.parseInt(rateVal) >= 1) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR, "发帖太频繁，请稍后再试");
        }
        msg.setUserId(loginUser.getUserId());
        // 生成ID并发MQ
        Long postId = stringRedisTemplate.opsForValue().increment("post:id:seq");
        msg.setPostId(postId);
        rabbitTemplate.convertAndSend(
                RabbitMQConfiguration.POST_CREATE_EXCHANGE,
                RabbitMQConfiguration.POST_CREATE_ROUTING_KEY,
                msg);
        // 设置限流
        stringRedisTemplate.opsForValue().set(rateKey, "1", 60, TimeUnit.SECONDS);
        return Result.ok(postId);
    }

    @Operation(summary = "分页获取帖子列表")
    @GetMapping("page")
    public Result<IPage<Map<String, Object>>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String sort) {
        QueryWrapper<PostInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("status", PostStatus.NORMAL.getCode());
        if ("hot".equals(sort)) {
            wrapper.orderByDesc("(like_count + IFNULL(comment_count, 0))");
        } else if ("rating".equals(sort)) {
            wrapper.ge("rating", 4);
            wrapper.orderByDesc("rating").orderByDesc("create_time");
        } else if ("bad".equals(sort)) {
            wrapper.le("rating", 3);
            wrapper.orderByDesc("create_time");
        } else {
            wrapper.orderByDesc("create_time");
        }
        IPage<PostInfo> postPage = postService.page(new Page<>(current, size), wrapper);

        // 封装返回结果（加上发帖人昵称和头像）
        IPage<Map<String, Object>> resultPage = postPage.convert(post -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", post.getId());
            map.put("userId", post.getUserId());
            map.put("title", post.getTitle());
            map.put("content", post.getContent());
            map.put("images", post.getImages());
            map.put("rating", post.getRating());
            map.put("likeCount", post.getLikeCount());
            map.put("commentCount", post.getCommentCount());
            map.put("apartmentId", post.getApartmentId());
            map.put("roomId", post.getRoomId());
            map.put("createTime", post.getCreateTime());
            // 查发帖人信息
            UserInfo user = userInfoMapper.selectById(post.getUserId());
            if (user != null) {
                map.put("nickname", user.getNickname());
                map.put("avatarUrl", user.getAvatarUrl());
            }
            // 查当前用户是否点赞
            LoginUser loginUser = LoginUserHolder.getLoginUser();
            if (loginUser != null) {
                String likeKey = RedisConstant.POST_LIKE_USERS_PREFIX + post.getId() + RedisConstant.POST_LIKE_USERS_SUFFIX;
                Boolean isLiked = stringRedisTemplate.opsForSet().isMember(likeKey, String.valueOf(loginUser.getUserId()));
                map.put("isLiked", Boolean.TRUE.equals(isLiked));
            } else {
                map.put("isLiked", false);
            }
            return map;
        });
        return Result.ok(resultPage);
    }

    @Operation(summary = "获取帖子详情")
    @GetMapping("detail")
    public Result<Map<String, Object>> detail(@RequestParam Long id) {
        PostInfo post = postService.getById(id);
        if (post == null || !PostStatus.NORMAL.equals(post.getStatus())) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR, "帖子不存在或已被屏蔽");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("post", post);
        UserInfo user = userInfoMapper.selectById(post.getUserId());
        if (user != null) {
            map.put("nickname", user.getNickname());
            map.put("avatarUrl", user.getAvatarUrl());
        }
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        if (loginUser != null) {
            String likeKey = RedisConstant.POST_LIKE_USERS_PREFIX + id + RedisConstant.POST_LIKE_USERS_SUFFIX;
            Boolean isLiked = stringRedisTemplate.opsForSet().isMember(likeKey, String.valueOf(loginUser.getUserId()));
            map.put("isLiked", Boolean.TRUE.equals(isLiked));
        }
        return Result.ok(map);
    }

    @Operation(summary = "获取我的帖子列表")
    @GetMapping("myPosts")
    public Result<List<PostInfo>> myPosts() {
        LoginUser loginUser = getCurrentUser();
        LambdaQueryWrapper<PostInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostInfo::getUserId, loginUser.getUserId());
        wrapper.orderByDesc(PostInfo::getCreateTime);
        return Result.ok(postService.list(wrapper));
    }
}
