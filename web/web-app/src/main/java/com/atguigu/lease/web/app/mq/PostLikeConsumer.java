package com.atguigu.lease.web.app.mq;

import com.atguigu.lease.common.mq.RabbitMQConfiguration;
import com.atguigu.lease.common.mq.dto.NotificationMessage;
import com.atguigu.lease.common.mq.dto.PostLikeMessage;
import com.atguigu.lease.model.entity.PostInfo;
import com.atguigu.lease.model.entity.PostLike;
import com.atguigu.lease.model.entity.UserInfo;
import com.atguigu.lease.web.app.mapper.UserInfoMapper;
import com.atguigu.lease.web.app.service.PostLikeService;
import com.atguigu.lease.web.app.service.PostService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class PostLikeConsumer {

    @Autowired
    private PostLikeService postLikeService;

    @Autowired
    private PostService postService;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfiguration.POST_LIKE_QUEUE)
    public void onMessage(PostLikeMessage msg,
                          Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            if ("like".equals(msg.getAction())) {
                PostLike like = new PostLike();
                like.setPostId(msg.getPostId());
                like.setUserId(msg.getUserId());
                postLikeService.save(like);

                // 通知帖子作者
                PostInfo post = postService.getById(msg.getPostId());
                if (post != null && !post.getUserId().equals(msg.getUserId())) {
                    UserInfo liker = userInfoMapper.selectById(msg.getUserId());
                    String likerName = liker != null ? liker.getNickname() : "某人";
                    String title = post.getTitle();
                    if (title != null && title.length() > 10) {
                        title = title.substring(0, 10) + "...";
                    }
                    NotificationMessage notif = new NotificationMessage(
                            post.getUserId(),
                            "新点赞",
                            likerName + " 赞了你的评价「" + title + "」",
                            6, msg.getPostId());
                    rabbitTemplate.convertAndSend(
                            RabbitMQConfiguration.NOTIFICATION_EXCHANGE,
                            RabbitMQConfiguration.NOTIFICATION_ROUTING_KEY,
                            notif);
                }
            } else {
                // unlike
                LambdaQueryWrapper<PostLike> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(PostLike::getPostId, msg.getPostId());
                wrapper.eq(PostLike::getUserId, msg.getUserId());
                postLikeService.remove(wrapper);
            }
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("点赞落库失败: {}", e.getMessage(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
