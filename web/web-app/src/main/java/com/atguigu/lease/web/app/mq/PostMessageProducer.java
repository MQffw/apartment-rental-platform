package com.atguigu.lease.web.app.mq;

import com.atguigu.lease.common.mq.RabbitMQConfiguration;
import com.atguigu.lease.common.mq.dto.PostCreateMessage;
import com.atguigu.lease.common.mq.dto.PostLikeMessage;
import com.atguigu.lease.common.mq.dto.PostCommentMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PostMessageProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendCreatePost(PostCreateMessage msg) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfiguration.POST_CREATE_EXCHANGE,
                RabbitMQConfiguration.POST_CREATE_ROUTING_KEY,
                msg);
    }

    public void sendLikePost(PostLikeMessage msg) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfiguration.POST_LIKE_EXCHANGE,
                RabbitMQConfiguration.POST_LIKE_ROUTING_KEY,
                msg);
    }

    public void sendCommentPost(PostCommentMessage msg) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfiguration.POST_COMMENT_EXCHANGE,
                RabbitMQConfiguration.POST_COMMENT_ROUTING_KEY,
                msg);
    }
}
