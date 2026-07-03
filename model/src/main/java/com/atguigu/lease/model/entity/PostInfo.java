package com.atguigu.lease.model.entity;

import com.atguigu.lease.model.enums.PostStatus;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "帖子/点评表")
@TableName(value = "post_info")
@Data
public class PostInfo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "发帖人ID")
    @TableField(value = "user_id")
    private Long userId;

    @Schema(description = "关联公寓ID")
    @TableField(value = "apartment_id")
    private Long apartmentId;

    @Schema(description = "关联房间ID")
    @TableField(value = "room_id")
    private Long roomId;

    @Schema(description = "标题")
    @TableField(value = "title")
    private String title;

    @Schema(description = "正文")
    @TableField(value = "content")
    private String content;

    @Schema(description = "图片URL,JSON数组")
    @TableField(value = "images")
    private String images;

    @Schema(description = "评分 1-5")
    @TableField(value = "rating")
    private Integer rating;

    @Schema(description = "点赞数")
    @TableField(value = "like_count")
    private Integer likeCount;

    @Schema(description = "评论数")
    @TableField(value = "comment_count")
    private Integer commentCount;

    @Schema(description = "状态：1-正常 2-审核中 3-已屏蔽")
    @TableField(value = "status")
    private PostStatus status;
}
