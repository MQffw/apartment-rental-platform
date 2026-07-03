package com.atguigu.lease.web.app.mq;

import com.atguigu.lease.common.mq.RabbitMQConfiguration;
import com.atguigu.lease.common.mq.dto.NotificationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationMessageProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendNotification(Long userId, String title, String content, Integer type, Long relatedId) {
        NotificationMessage msg = new NotificationMessage(userId, title, content, type, relatedId);
        rabbitTemplate.convertAndSend(
                RabbitMQConfiguration.NOTIFICATION_EXCHANGE,
                RabbitMQConfiguration.NOTIFICATION_ROUTING_KEY,
                msg
        );
        log.debug("发送通知消息: userId={}, title={}", userId, title);
    }
}
