package com.atguigu.lease.web.app.controller.payment;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.model.entity.PaymentRecord;
import com.atguigu.lease.model.enums.BillType;
import com.atguigu.lease.model.enums.PaymentStatus;
import com.atguigu.lease.model.enums.PaymentType;
import com.atguigu.lease.web.app.custom.LoginUser;
import com.atguigu.lease.web.app.custom.holder.LoginUserHolder;
import com.atguigu.lease.web.app.service.PaymentRecordService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "支付记录信息")
@RestController
@RequestMapping("/app/paymentRecord")
@Slf4j
public class PaymentRecordController {

    @Autowired
    private PaymentRecordService service;

    private Long getCurrentUserId() {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_AUTH);
        }
        return loginUser.getUserId();
    }

    @Operation(summary = "查询个人支付记录列表")
    @GetMapping("listByAgreementId")
    public Result<List<PaymentRecord>> listByAgreementId(@RequestParam Long agreementId) {
        if (agreementId == null || agreementId <= 0) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR, "租约ID不能为空");
        }
        Long userId = getCurrentUserId();
        LambdaQueryWrapper<PaymentRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PaymentRecord::getAgreementId, agreementId);
        queryWrapper.eq(PaymentRecord::getUserId, userId);
        queryWrapper.orderByDesc(PaymentRecord::getCreateTime);
        List<PaymentRecord> list = service.list(queryWrapper);
        return Result.ok(list);
    }

    @Operation(summary = "查询当前用户指定租约的全部账单")
    @GetMapping("bills")
    public Result<List<PaymentRecord>> bills(@RequestParam Long agreementId) {
        Long userId = getCurrentUserId();
        LambdaQueryWrapper<PaymentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentRecord::getAgreementId, agreementId);
        wrapper.eq(PaymentRecord::getUserId, userId);
        wrapper.orderByDesc(PaymentRecord::getCreateTime);
        return Result.ok(service.list(wrapper));
    }

    @Operation(summary = "支付账单")
    @PostMapping("pay/{id}")
    public Result<Void> pay(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        PaymentRecord record = service.getById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR, "账单不存在");
        }
        if (record.getPaymentStatus() == PaymentStatus.PAID) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR, "该账单已支付");
        }
        record.setPaymentStatus(PaymentStatus.PAID);
        record.setPaymentTime(new Date());
        service.updateById(record);
        return Result.ok();
    }

    @Operation(summary = "创建水电费账单")
    @PostMapping("createBill")
    public Result<Void> createBill(@RequestParam Long agreementId,
                                   @RequestParam Integer billType,
                                   @RequestParam BigDecimal amount) {
        Long userId = getCurrentUserId();
        PaymentRecord record = new PaymentRecord();
        record.setAgreementId(agreementId);
        record.setUserId(userId);
        record.setAmount(amount);
        record.setPaymentType(PaymentType.RENT);
        record.setPaymentStatus(PaymentStatus.PENDING);
        record.setBillType(BillType.values()[billType - 1]);
        service.save(record);
        return Result.ok();
    }
}
