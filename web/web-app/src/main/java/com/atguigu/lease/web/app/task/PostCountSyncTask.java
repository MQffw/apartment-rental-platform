package com.atguigu.lease.web.app.task;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.model.entity.PostInfo;
import com.atguigu.lease.web.app.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class PostCountSyncTask {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private PostService postService;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 每5分钟将Redis中的点赞数回写到数据库
     */
    @Scheduled(fixedRate = 300000)
    public void syncLikeCounts() {
        syncCounts("syncLikeCounts", RedisConstant.POST_LIKE_USERS_PREFIX,
                RedisConstant.POST_LIKE_COUNT_SUFFIX, true);
    }

    /**
     * 每5分钟同步评论数（延迟150秒启动，与点赞任务错峰）
     */
    @Scheduled(fixedRate = 300000, initialDelay = 150000)
    public void syncCommentCounts() {
        syncCounts("syncCommentCounts", RedisConstant.POST_COMMENT_COUNT_PREFIX,
                RedisConstant.POST_COMMENT_COUNT_SUFFIX, false);
    }

    /**
     * 计数回写通用逻辑，加分布式锁防止多实例重复回写
     */
    private void syncCounts(String taskName, String keyPrefix, String keySuffix, boolean like) {
        RLock lock = redissonClient.getLock(RedisConstant.TASK_LOCK_PREFIX + "app:" + taskName);
        boolean locked = false;
        try {
            locked = lock.tryLock(0, 240, TimeUnit.SECONDS);
            if (!locked) {
                log.info("其他实例正在执行 {}，本实例跳过", taskName);
                return;
            }
            Set<String> countKeys = stringRedisTemplate.keys(keyPrefix + "*" + keySuffix);
            if (countKeys == null || countKeys.isEmpty()) return;

            int updated = 0;
            for (String key : countKeys) {
                try {
                    String postIdStr = key.replace(keyPrefix, "").replace(keySuffix, "");
                    Long postId = Long.parseLong(postIdStr);
                    String countStr = stringRedisTemplate.opsForValue().get(key);
                    if (countStr != null) {
                        int count = Integer.parseInt(countStr);
                        PostInfo post = new PostInfo();
                        post.setId(postId);
                        if (like) {
                            post.setLikeCount(count);
                        } else {
                            post.setCommentCount(count);
                        }
                        postService.updateById(post);
                        updated++;
                    }
                } catch (Exception e) {
                    log.warn("{} 回写失败: key={}, error={}", taskName, key, e.getMessage());
                }
            }
            log.info("{} 完成: 更新了 {} 条记录", taskName, updated);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
