package com.atguigu.lease.model.entity;

import com.atguigu.lease.model.enums.NotificationType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "通知消息表")
@TableName(value = "notification")
@Data
public class Notification extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "接收用户ID")
    @TableField(value = "user_id")
    private Long userId;

    @Schema(description = "通知标题")
    @TableField(value = "title")
    private String title;

    @Schema(description = "通知内容")
    @TableField(value = "content")
    private String content;

    @Schema(description = "通知类型：1-租约到期提醒 2-预约状态变更 3-新预约通知 4-租约状态变更")
    @TableField(value = "type")
    private NotificationType type;

    @Schema(description = "是否已读：0-未读 1-已读")
    @TableField(value = "is_read")
    private Integer isRead;

    @Schema(description = "关联业务ID")
    @TableField(value = "related_id")
    private Long relatedId;
}
