package com.atguigu.lease.web.admin.schedule;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.model.entity.LeaseAgreement;
import com.atguigu.lease.model.enums.LeaseStatus;
import com.atguigu.lease.web.admin.service.LeaseAgreementService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ScheduledTasks {

    @Autowired
    private LeaseAgreementService leaseAgreementService;

    @Autowired
    private RedissonClient redissonClient;

    @Scheduled(cron = "0 0 0 * * *")
    public void checkLeaseStatus() {
        // 分布式锁：多实例部署时保证任务只执行一次（等锁0秒、持锁上限300秒自动释放）
        RLock lock = redissonClient.getLock(RedisConstant.TASK_LOCK_PREFIX + "admin:checkLeaseStatus");
        boolean locked = false;
        try {
            locked = lock.tryLock(0, 300, TimeUnit.SECONDS);
            if (!locked) {
                log.info("其他实例正在执行租约到期检查，本实例跳过");
                return;
            }
            LambdaUpdateWrapper<LeaseAgreement> updateWrapper = new LambdaUpdateWrapper<>();
            Date now = new Date();
            updateWrapper.le(LeaseAgreement::getLeaseEndDate, now);
            updateWrapper.in(LeaseAgreement::getStatus, LeaseStatus.SIGNED, LeaseStatus.WITHDRAWING);
            updateWrapper.set(LeaseAgreement::getStatus, LeaseStatus.EXPIRED);

            boolean updated = leaseAgreementService.update(updateWrapper);
            log.info("定时任务: 租约过期检查完成，截止日期={}, 执行结果={}", now, updated);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
