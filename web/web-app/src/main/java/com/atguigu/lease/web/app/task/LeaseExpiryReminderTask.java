package com.atguigu.lease.web.app.task;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.model.entity.LeaseAgreement;
import com.atguigu.lease.model.enums.LeaseStatus;
import com.atguigu.lease.web.app.mapper.LeaseAgreementMapper;
import com.atguigu.lease.web.app.mq.NotificationMessageProducer;
import com.atguigu.lease.web.app.mapper.UserInfoMapper;
import com.atguigu.lease.model.entity.UserInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class LeaseExpiryReminderTask {

    @Autowired
    private LeaseAgreementMapper leaseAgreementMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private NotificationMessageProducer notificationProducer;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 每天早8点执行，查询7天内到期的租约并发送提醒
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void sendLeaseExpiryReminder() {
        // 分布式锁：多实例部署时保证任务只执行一次
        RLock lock = redissonClient.getLock(RedisConstant.TASK_LOCK_PREFIX + "app:leaseExpiryReminder");
        boolean locked = false;
        try {
            locked = lock.tryLock(0, 600, TimeUnit.SECONDS);
            if (!locked) {
                log.info("其他实例正在执行租约到期提醒，本实例跳过");
                return;
            }
            Calendar cal = Calendar.getInstance();
            Date now = cal.getTime();
            cal.add(Calendar.DAY_OF_MONTH, 7);
            Date sevenDaysLater = cal.getTime();

            LambdaQueryWrapper<LeaseAgreement> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(LeaseAgreement::getStatus, LeaseStatus.SIGNED);
            wrapper.between(LeaseAgreement::getLeaseEndDate, now, sevenDaysLater);
            List<LeaseAgreement> expiringLeases = leaseAgreementMapper.selectList(wrapper);

            log.info("租约到期提醒: 查询到 {} 条即将到期的租约", expiringLeases.size());

            for (LeaseAgreement agreement : expiringLeases) {
                try {
                    UserInfo user = userInfoMapper.selectOne(
                        new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getEmail, agreement.getEmail()));
                    if (user != null) {
                        notificationProducer.sendNotification(user.getId(),
                            "租约到期提醒",
                            "您的租约将于" + agreement.getLeaseEndDate() + "到期，请及时处理",
                            1, agreement.getId());
                    }
                } catch (Exception e) {
                    log.warn("发送租约到期提醒失败: agreementId={}, error={}", agreement.getId(), e.getMessage());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
