package com.atguigu.lease.web.app.task;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.model.entity.BrowsingHistory;
import com.atguigu.lease.web.app.mapper.BrowsingHistoryMapper;
import com.atguigu.lease.web.app.service.BrowsingHistoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 浏览历史自动清理任务
 * 保留策略：仅保留最近 30 天的浏览记录，超过的每日凌晨 3 点自动清除（逻辑删除）。
 * 列表查询（BrowsingHistoryMapper.pageItemByUserId）同步只展示近 30 天，
 * 清理任务删除后数据彻底落库，两端保持一致。
 */
@Component
@Slf4j
public class BrowsingHistoryCleanTask {

    /**
     * 浏览历史保留时长（天）
     */
    private static final int RETENTION_DAYS = 30;

    @Autowired
    private BrowsingHistoryService browsingHistoryService;

    @Autowired
    private BrowsingHistoryMapper browsingHistoryMapper;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 每天凌晨 3 点清理超过 30 天的浏览历史，加分布式锁防止多实例重复清理
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanExpiredHistory() {
        RLock lock = redissonClient.getLock(RedisConstant.TASK_LOCK_PREFIX + "app:cleanBrowsingHistory");
        boolean locked = false;
        try {
            locked = lock.tryLock(0, 300, java.util.concurrent.TimeUnit.SECONDS);
            if (!locked) {
                log.info("其他实例正在执行浏览历史清理，本实例跳过");
                return;
            }
            Date cutoff = new Date(System.currentTimeMillis() - RETENTION_DAYS * 24L * 3600 * 1000);
            LambdaQueryWrapper<BrowsingHistory> wrapper = new LambdaQueryWrapper<>();
            wrapper.lt(BrowsingHistory::getBrowseTime, cutoff);
            int removed = browsingHistoryMapper.delete(wrapper);
            log.info("cleanExpiredHistory 完成: 清理了 {} 条超过 {} 天的浏览历史", removed, RETENTION_DAYS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
