package com.atguigu.lease.common.mq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfiguration {

    // ==================== JSON 消息转换器 ====================

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setPrefetchCount(10);
        return factory;
    }

    // ==================== 通知队列 ====================

    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String NOTIFICATION_ROUTING_KEY = "notification.send";
    public static final String NOTIFICATION_DEAD_EXCHANGE = "notification.dead.exchange";
    public static final String NOTIFICATION_DEAD_QUEUE = "notification.dead.queue";

    @Bean
    public DirectExchange notificationExchange() {
        return ExchangeBuilder.directExchange(NOTIFICATION_EXCHANGE).durable(true).build();
    }

    @Bean
    public DirectExchange notificationDeadExchange() {
        return ExchangeBuilder.directExchange(NOTIFICATION_DEAD_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", NOTIFICATION_DEAD_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "notification.dead")
                .build();
    }

    @Bean
    public Queue notificationDeadQueue() {
        return QueueBuilder.durable(NOTIFICATION_DEAD_QUEUE).build();
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue()).to(notificationExchange()).with(NOTIFICATION_ROUTING_KEY);
    }

    @Bean
    public Binding notificationDeadBinding() {
        return BindingBuilder.bind(notificationDeadQueue()).to(notificationDeadExchange()).with("notification.dead");
    }

    // ==================== 帖子发布队列 ====================

    public static final String POST_CREATE_EXCHANGE = "post.create.exchange";
    public static final String POST_CREATE_QUEUE = "post.create.queue";
    public static final String POST_CREATE_ROUTING_KEY = "post.create";
    public static final String POST_CREATE_DEAD_EXCHANGE = "post.create.dead.exchange";
    public static final String POST_CREATE_DEAD_QUEUE = "post.create.dead.queue";

    @Bean
    public DirectExchange postCreateExchange() {
        return ExchangeBuilder.directExchange(POST_CREATE_EXCHANGE).durable(true).build();
    }

    @Bean
    public DirectExchange postCreateDeadExchange() {
        return ExchangeBuilder.directExchange(POST_CREATE_DEAD_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue postCreateQueue() {
        return QueueBuilder.durable(POST_CREATE_QUEUE)
                .withArgument("x-dead-letter-exchange", POST_CREATE_DEAD_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "post.create.dead")
                .build();
    }

    @Bean
    public Queue postCreateDeadQueue() {
        return QueueBuilder.durable(POST_CREATE_DEAD_QUEUE).build();
    }

    @Bean
    public Binding postCreateBinding() {
        return BindingBuilder.bind(postCreateQueue()).to(postCreateExchange()).with(POST_CREATE_ROUTING_KEY);
    }

    @Bean
    public Binding postCreateDeadBinding() {
        return BindingBuilder.bind(postCreateDeadQueue()).to(postCreateDeadExchange()).with("post.create.dead");
    }

    // ==================== 点赞队列 ====================

    public static final String POST_LIKE_EXCHANGE = "post.like.exchange";
    public static final String POST_LIKE_QUEUE = "post.like.queue";
    public static final String POST_LIKE_ROUTING_KEY = "post.like";
    public static final String POST_LIKE_DEAD_EXCHANGE = "post.like.dead.exchange";
    public static final String POST_LIKE_DEAD_QUEUE = "post.like.dead.queue";

    @Bean
    public DirectExchange postLikeExchange() {
        return ExchangeBuilder.directExchange(POST_LIKE_EXCHANGE).durable(true).build();
    }

    @Bean
    public DirectExchange postLikeDeadExchange() {
        return ExchangeBuilder.directExchange(POST_LIKE_DEAD_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue postLikeQueue() {
        return QueueBuilder.durable(POST_LIKE_QUEUE)
                .withArgument("x-dead-letter-exchange", POST_LIKE_DEAD_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "post.like.dead")
                .build();
    }

    @Bean
    public Queue postLikeDeadQueue() {
        return QueueBuilder.durable(POST_LIKE_DEAD_QUEUE).build();
    }

    @Bean
    public Binding postLikeBinding() {
        return BindingBuilder.bind(postLikeQueue()).to(postLikeExchange()).with(POST_LIKE_ROUTING_KEY);
    }

    @Bean
    public Binding postLikeDeadBinding() {
        return BindingBuilder.bind(postLikeDeadQueue()).to(postLikeDeadExchange()).with("post.like.dead");
    }

    // ==================== 评论队列 ====================

    public static final String POST_COMMENT_EXCHANGE = "post.comment.exchange";
    public static final String POST_COMMENT_QUEUE = "post.comment.queue";
    public static final String POST_COMMENT_ROUTING_KEY = "post.comment";
    public static final String POST_COMMENT_DEAD_EXCHANGE = "post.comment.dead.exchange";
    public static final String POST_COMMENT_DEAD_QUEUE = "post.comment.dead.queue";

    @Bean
    public DirectExchange postCommentExchange() {
        return ExchangeBuilder.directExchange(POST_COMMENT_EXCHANGE).durable(true).build();
    }

    @Bean
    public DirectExchange postCommentDeadExchange() {
        return ExchangeBuilder.directExchange(POST_COMMENT_DEAD_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue postCommentQueue() {
        return QueueBuilder.durable(POST_COMMENT_QUEUE)
                .withArgument("x-dead-letter-exchange", POST_COMMENT_DEAD_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "post.comment.dead")
                .build();
    }

    @Bean
    public Queue postCommentDeadQueue() {
        return QueueBuilder.durable(POST_COMMENT_DEAD_QUEUE).build();
    }

    @Bean
    public Binding postCommentBinding() {
        return BindingBuilder.bind(postCommentQueue()).to(postCommentExchange()).with(POST_COMMENT_ROUTING_KEY);
    }

    @Bean
    public Binding postCommentDeadBinding() {
        return BindingBuilder.bind(postCommentDeadQueue()).to(postCommentDeadExchange()).with("post.comment.dead");
    }
}
