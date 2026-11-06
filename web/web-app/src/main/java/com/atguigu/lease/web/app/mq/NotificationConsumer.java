package com.atguigu.lease.web.app.mq;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.mq.RabbitMQConfiguration;
import com.atguigu.lease.common.mq.dto.NotificationMessage;
import com.atguigu.lease.model.entity.Notification;
import com.atguigu.lease.model.enums.NotificationType;
import com.atguigu.lease.web.app.service.NotificationService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class NotificationConsumer {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @RabbitListener(queues = RabbitMQConfiguration.NOTIFICATION_QUEUE)
    public void onMessage(NotificationMessage msg,
                          Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        // 幂等检查：基于 msgId 去重，重投递消息直接 ack 跳过
        String dedupeKey = RedisConstant.MQ_IDEMPOTENT_PREFIX + "notification:" + msg.getMsgId();
        Boolean first = stringRedisTemplate.opsForValue().setIfAbsent(
                dedupeKey, "1", RedisConstant.MQ_IDEMPOTENT_TTL_HOURS, TimeUnit.HOURS);
        if (!Boolean.TRUE.equals(first)) {
            channel.basicAck(deliveryTag, false);
            log.info("重复消息已跳过: key={}", dedupeKey);
            return;
        }
        try {
            Notification notification = new Notification();
            notification.setUserId(msg.getUserId());
            notification.setTitle(msg.getTitle());
            notification.setContent(msg.getContent());
            notification.setType(NotificationType.values()[msg.getType() - 1]);
            notification.setIsRead(0);
            notification.setIsDeleted((byte) 0);
            notification.setRelatedId(msg.getRelatedId());
            notificationService.save(notification);

            // INCR Redis未读数
            String key = RedisConstant.NOTIFICATION_UNREAD_PREFIX + msg.getUserId();
            stringRedisTemplate.opsForValue().increment(key);

            channel.basicAck(deliveryTag, false);
            log.info("通知消费成功: userId={}, title={}", msg.getUserId(), msg.getTitle());
        } catch (Exception e) {
            log.error("通知消费失败: {}", e.getMessage(), e);
            // nack + 不重入队列（进入死信队列）
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
