package com.atguigu.lease.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "帖子点赞记录表")
@TableName(value = "post_like")
@Data
public class PostLike extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "帖子ID")
    @TableField(value = "post_id")
    private Long postId;

    @Schema(description = "用户ID")
    @TableField(value = "user_id")
    private Long userId;
}
