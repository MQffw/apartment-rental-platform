package com.atguigu.lease.common.mq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostCommentMessage {

    private Long commentId;
    private Long postId;
    private Long userId;
    private Long parentId;
    private Long replyToId;
    private String content;
}
