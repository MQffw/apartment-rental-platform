package com.atguigu.lease.common.mq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostLikeMessage {

    private Long postId;
    private Long userId;

    /** "like" 或 "unlike" */
    private String action;
}
