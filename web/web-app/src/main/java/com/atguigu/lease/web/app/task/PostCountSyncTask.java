package com.atguigu.lease.web.app.task;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.model.entity.PostInfo;
import com.atguigu.lease.web.app.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
@Slf4j
public class PostCountSyncTask {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private PostService postService;

    /**
     * 每5分钟将Redis中的点赞数和评论数回写到数据库
     */
    @Scheduled(fixedRate = 300000)
    public void syncLikeCounts() {
        Set<String> countKeys = stringRedisTemplate.keys(RedisConstant.POST_LIKE_USERS_PREFIX + "*" + RedisConstant.POST_LIKE_COUNT_SUFFIX);
        if (countKeys == null || countKeys.isEmpty()) return;

        int updated = 0;
        for (String key : countKeys) {
            try {
                String postIdStr = key.replace(RedisConstant.POST_LIKE_USERS_PREFIX, "")
                        .replace(RedisConstant.POST_LIKE_COUNT_SUFFIX, "");
                Long postId = Long.parseLong(postIdStr);
                String countStr = stringRedisTemplate.opsForValue().get(key);
                if (countStr != null) {
                    int count = Integer.parseInt(countStr);
                    PostInfo post = new PostInfo();
                    post.setId(postId);
                    post.setLikeCount(count);
                    postService.updateById(post);
                    updated++;
                }
            } catch (Exception e) {
                log.warn("同步点赞数失败: key={}, error={}", key, e.getMessage());
            }
        }
        log.info("点赞数回写完成: 更新了 {} 条记录", updated);
    }

    /**
     * 每5分钟同步评论数
     */
    @Scheduled(fixedRate = 300000, initialDelay = 150000)
    public void syncCommentCounts() {
        Set<String> countKeys = stringRedisTemplate.keys(RedisConstant.POST_COMMENT_COUNT_PREFIX + "*" + RedisConstant.POST_COMMENT_COUNT_SUFFIX);
        if (countKeys == null || countKeys.isEmpty()) return;

        int updated = 0;
        for (String key : countKeys) {
            try {
                String postIdStr = key.replace(RedisConstant.POST_COMMENT_COUNT_PREFIX, "")
                        .replace(RedisConstant.POST_COMMENT_COUNT_SUFFIX, "");
                Long postId = Long.parseLong(postIdStr);
                String countStr = stringRedisTemplate.opsForValue().get(key);
                if (countStr != null) {
                    int count = Integer.parseInt(countStr);
                    PostInfo post = new PostInfo();
                    post.setId(postId);
                    post.setCommentCount(count);
                    postService.updateById(post);
                    updated++;
                }
            } catch (Exception e) {
                log.warn("同步评论数失败: key={}, error={}", key, e.getMessage());
            }
        }
        log.info("评论数回写完成: 更新了 {} 条记录", updated);
    }
}
