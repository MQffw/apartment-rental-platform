package com.atguigu.lease.web.app.mq;

import com.atguigu.lease.common.mq.dto.PostCreateMessage;
import com.atguigu.lease.model.entity.PostInfo;
import com.atguigu.lease.web.app.service.PostService;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 帖子发 MQ 消费幂等测试：RabbitMQ 重投递同一消息时不得重复入库
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("帖子消费幂等")
class PostCreateConsumerTest {

    @Mock
    private PostService postService;
    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private Channel channel;

    @InjectMocks
    private PostCreateConsumer consumer;

    private PostCreateMessage msg;
    private static final long DELIVERY_TAG = 1L;

    @BeforeEach
    void setUp() {
        msg = new PostCreateMessage();
        msg.setPostId(666L);
        msg.setUserId(100L);
        msg.setTitle("性价比很高的公寓");
        msg.setContent("住了半年，体验不错");
        msg.setRating(5);
    }

    @Test
    @DisplayName("首次消费：帖子落库并 ack")
    void firstConsume_savesAndAcks() throws Exception {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                .thenReturn(true);

        consumer.onMessage(msg, channel, DELIVERY_TAG);

        verify(postService).save(any(PostInfo.class));
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("重复投递：直接 ack 跳过，不重复入库")
    void duplicateConsume_skipsWithoutSaving() throws Exception {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        // 幂等键已存在 → 判定为重复消息
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                .thenReturn(false);

        consumer.onMessage(msg, channel, DELIVERY_TAG);

        verify(postService, never()).save(any());
        verify(channel).basicAck(DELIVERY_TAG, false);
        verify(channel, never()).basicNack(anyLong(), eq(false), eq(false));
    }

    @Test
    @DisplayName("落库异常：nack 进入死信队列（不重回队列）")
    void saveFails_nacksToDeadLetter() throws Exception {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                .thenReturn(true);
        doThrow(new RuntimeException("DB down")).when(postService).save(any(PostInfo.class));

        consumer.onMessage(msg, channel, DELIVERY_TAG);

        verify(channel).basicNack(DELIVERY_TAG, false, false);
    }
}
