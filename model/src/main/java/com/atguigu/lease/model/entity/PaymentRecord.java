package com.atguigu.lease.model.entity;

import com.atguigu.lease.model.enums.BillType;
import com.atguigu.lease.model.enums.PaymentStatus;
import com.atguigu.lease.model.enums.PaymentType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Schema(description = "支付记录表")
@TableName(value = "payment_record")
@Data
public class PaymentRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "关联租约ID")
    @TableField(value = "agreement_id")
    private Long agreementId;

    @Schema(description = "用户ID")
    @TableField(value = "user_id")
    private Long userId;

    @Schema(description = "支付金额")
    @TableField(value = "amount")
    private BigDecimal amount;

    @Schema(description = "支付类型：1-租金 2-押金")
    @TableField(value = "payment_type")
    private PaymentType paymentType;

    @Schema(description = "支付状态：0-待支付 1-已支付 2-支付失败")
    @TableField(value = "payment_status")
    private PaymentStatus paymentStatus;

    @Schema(description = "账单类型：1-房租 2-电费 3-水费 4-物业费")
    @TableField(value = "bill_type")
    private BillType billType;

    @Schema(description = "支付时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "payment_time")
    private Date paymentTime;
}
