package com.atguigu.lease.common.mq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostCreateMessage {

    private Long postId;
    private Long userId;
    private String title;
    private String content;
    private String images;
    private Integer rating;
    private Long apartmentId;
    private Long roomId;
}
