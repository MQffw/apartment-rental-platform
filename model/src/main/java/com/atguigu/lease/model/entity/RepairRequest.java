package com.atguigu.lease.model.entity;

import com.atguigu.lease.model.enums.RepairStatus;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "报修工单表")
@TableName(value = "repair_request")
@Data
public class RepairRequest extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    @TableField(value = "user_id")
    private Long userId;

    @Schema(description = "房间ID")
    @TableField(value = "room_id")
    private Long roomId;

    @Schema(description = "公寓ID")
    @TableField(value = "apartment_id")
    private Long apartmentId;

    @Schema(description = "报修标题")
    @TableField(value = "title")
    private String title;

    @Schema(description = "问题描述")
    @TableField(value = "description")
    private String description;

    @Schema(description = "图片URL,逗号分隔")
    @TableField(value = "images")
    private String images;

    @Schema(description = "状态：0-待处理 1-处理中 2-已完成 3-已关闭")
    @TableField(value = "status")
    private RepairStatus status;
}
