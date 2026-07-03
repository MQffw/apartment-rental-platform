package com.atguigu.lease.web.app.mq;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.mq.RabbitMQConfiguration;
import com.atguigu.lease.common.mq.dto.NotificationMessage;
import com.atguigu.lease.common.mq.dto.PostCommentMessage;
import com.atguigu.lease.model.entity.PostComment;
import com.atguigu.lease.model.entity.PostInfo;
import com.atguigu.lease.model.entity.UserInfo;
import com.atguigu.lease.web.app.mapper.UserInfoMapper;
import com.atguigu.lease.web.app.service.PostCommentService;
import com.atguigu.lease.web.app.service.PostService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class PostCommentConsumer {

    @Autowired
    private PostCommentService commentService;

    @Autowired
    private PostService postService;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfiguration.POST_COMMENT_QUEUE)
    public void onMessage(PostCommentMessage msg,
                          Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            PostComment comment = new PostComment();
            comment.setId(msg.getCommentId());
            comment.setPostId(msg.getPostId());
            comment.setUserId(msg.getUserId());
            comment.setParentId(msg.getParentId() != null ? msg.getParentId() : 0L);
            comment.setReplyToId(msg.getReplyToId() != null ? msg.getReplyToId() : 0L);
            comment.setContent(msg.getContent());
            comment.setLikeCount(0);
            comment.setStatus(1);
            comment.setIsDeleted((byte) 0);
            commentService.save(comment);

            // INCR Redis评论数
            String countKey = RedisConstant.POST_COMMENT_COUNT_PREFIX + msg.getPostId() + RedisConstant.POST_COMMENT_COUNT_SUFFIX;
            stringRedisTemplate.opsForValue().increment(countKey);

            // 通知帖子作者
            PostInfo post = postService.getById(msg.getPostId());
            if (post != null && !post.getUserId().equals(msg.getUserId())) {
                UserInfo commenter = userInfoMapper.selectById(msg.getUserId());
                String commenterName = commenter != null ? commenter.getNickname() : "某人";
                String contentPreview = msg.getContent();
                if (contentPreview != null && contentPreview.length() > 20) {
                    contentPreview = contentPreview.substring(0, 20) + "...";
                }
                NotificationMessage notif = new NotificationMessage(
                        post.getUserId(),
                        "新评论",
                        commenterName + " 评论了你的评价：「" + contentPreview + "」",
                        7, msg.getPostId());
                rabbitTemplate.convertAndSend(
                        RabbitMQConfiguration.NOTIFICATION_EXCHANGE,
                        RabbitMQConfiguration.NOTIFICATION_ROUTING_KEY,
                        notif);
            }

            channel.basicAck(deliveryTag, false);
            log.info("评论入库成功: postId={}, commentId={}", msg.getPostId(), msg.getCommentId());
        } catch (Exception e) {
            log.error("评论入库失败: {}", e.getMessage(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
