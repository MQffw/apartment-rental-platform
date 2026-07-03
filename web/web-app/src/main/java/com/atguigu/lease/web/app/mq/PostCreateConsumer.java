package com.atguigu.lease.web.app.mq;

import com.atguigu.lease.common.mq.RabbitMQConfiguration;
import com.atguigu.lease.common.mq.dto.PostCreateMessage;
import com.atguigu.lease.model.entity.PostInfo;
import com.atguigu.lease.model.enums.PostStatus;
import com.atguigu.lease.web.app.service.PostService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class PostCreateConsumer {

    @Autowired
    private PostService postService;

    @RabbitListener(queues = RabbitMQConfiguration.POST_CREATE_QUEUE)
    public void onMessage(PostCreateMessage msg,
                          Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            PostInfo post = new PostInfo();
            post.setId(msg.getPostId());
            post.setUserId(msg.getUserId());
            post.setTitle(msg.getTitle());
            post.setContent(msg.getContent());
            post.setImages(msg.getImages());
            post.setRating(msg.getRating());
            post.setApartmentId(msg.getApartmentId());
            post.setRoomId(msg.getRoomId());
            post.setLikeCount(0);
            post.setCommentCount(0);
            post.setStatus(PostStatus.NORMAL);
            post.setIsDeleted((byte) 0);
            postService.save(post);

            channel.basicAck(deliveryTag, false);
            log.info("帖子入库成功: postId={}", msg.getPostId());
        } catch (Exception e) {
            log.error("帖子入库失败: {}", e.getMessage(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
