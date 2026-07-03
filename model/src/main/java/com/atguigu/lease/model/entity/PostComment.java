package com.atguigu.lease.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "帖子评论表")
@TableName(value = "post_comment")
@Data
public class PostComment extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "帖子ID")
    @TableField(value = "post_id")
    private Long postId;

    @Schema(description = "评论人ID")
    @TableField(value = "user_id")
    private Long userId;

    @Schema(description = "父评论ID（0=一级评论）")
    @TableField(value = "parent_id")
    private Long parentId;

    @Schema(description = "回复目标用户ID")
    @TableField(value = "reply_to_id")
    private Long replyToId;

    @Schema(description = "评论内容")
    @TableField(value = "content")
    private String content;

    @Schema(description = "点赞数")
    @TableField(value = "like_count")
    private Integer likeCount;

    @Schema(description = "状态：1-正常 3-已删除")
    @TableField(value = "status")
    private Integer status;
}
