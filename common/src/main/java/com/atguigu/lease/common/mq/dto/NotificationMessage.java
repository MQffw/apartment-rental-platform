package com.atguigu.lease.common.mq.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 站内通知消息。
 * msgId：消息唯一标识，用于消费端幂等去重（RabbitMQ 重投递时 msgId 不变）。
 * 兼容原有 5 参构造调用方：自动生成 msgId，无需改动既有生产者代码。
 */
@Data
@NoArgsConstructor
public class NotificationMessage {

    /** 消息唯一ID（消费幂等键） */
    private String msgId;

    /** 接收用户ID */
    private Long userId;

    /** 通知标题 */
    private String title;

    /** 通知内容 */
    private String content;

    /** 通知类型 1-7 */
    private Integer type;

    /** 关联业务ID */
    private Long relatedId;

    public NotificationMessage(Long userId, String title, String content, Integer type, Long relatedId) {
        this(UUID.randomUUID().toString().replace("-", ""), userId, title, content, type, relatedId);
    }

    public NotificationMessage(String msgId, Long userId, String title, String content,
                               Integer type, Long relatedId) {
        this.msgId = msgId;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.type = type;
        this.relatedId = relatedId;
    }
}
