package com.atguigu.lease.web.app.controller.repair;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.model.entity.LeaseAgreement;
import com.atguigu.lease.model.entity.RepairRequest;
import com.atguigu.lease.model.enums.LeaseStatus;
import com.atguigu.lease.web.app.custom.LoginUser;
import com.atguigu.lease.web.app.custom.holder.LoginUserHolder;
import com.atguigu.lease.web.app.service.RepairRequestService;
import com.atguigu.lease.web.app.service.LeaseAgreementService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "报修工单")
@RestController
@RequestMapping("/app/repair")
@Slf4j
public class RepairController {

    @Autowired
    private RepairRequestService repairService;

    @Autowired
    private LeaseAgreementService agreementService;

    private LoginUser getCurrentUser() {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_AUTH);
        }
        return loginUser;
    }

    @Operation(summary = "提交报修工单")
    @PostMapping("save")
    public Result save(@RequestBody RepairRequest repair) {
        LoginUser loginUser = getCurrentUser();
        // 查询当前用户的已签约租约，获取roomId和apartmentId
        LambdaQueryWrapper<LeaseAgreement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LeaseAgreement::getStatus, LeaseStatus.SIGNED);
        wrapper.last("LIMIT 1");
        LeaseAgreement agreement = agreementService.getOne(wrapper);
        if (agreement == null) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR, "您没有有效的租约");
        }
        repair.setUserId(loginUser.getUserId());
        repair.setRoomId(agreement.getRoomId());
        repair.setApartmentId(agreement.getApartmentId());
        repair.setStatus(com.atguigu.lease.model.enums.RepairStatus.PENDING);
        repairService.save(repair);
        return Result.ok();
    }

    @Operation(summary = "查询我的报修工单列表")
    @GetMapping("list")
    public Result<List<RepairRequest>> list() {
        LoginUser loginUser = getCurrentUser();
        LambdaQueryWrapper<RepairRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RepairRequest::getUserId, loginUser.getUserId());
        wrapper.orderByDesc(RepairRequest::getCreateTime);
        return Result.ok(repairService.list(wrapper));
    }
}
