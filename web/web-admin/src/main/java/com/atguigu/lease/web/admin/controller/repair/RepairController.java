package com.atguigu.lease.web.admin.controller.repair;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.mq.RabbitMQConfiguration;
import com.atguigu.lease.common.mq.dto.NotificationMessage;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.model.entity.RepairRequest;
import com.atguigu.lease.model.enums.RepairStatus;
import com.atguigu.lease.web.admin.service.RepairRequestService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "报修管理")
@RestController
@RequestMapping("/admin/repair")
@Slf4j
public class RepairController {

    @Autowired
    private RepairRequestService repairService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Operation(summary = "分页查询报修工单")
    @GetMapping("page")
    public Result<IPage<RepairRequest>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long apartmentId) {
        LambdaQueryWrapper<RepairRequest> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(RepairRequest::getStatus, status);
        }
        if (apartmentId != null) {
            wrapper.eq(RepairRequest::getApartmentId, apartmentId);
        }
        wrapper.orderByDesc(RepairRequest::getCreateTime);
        IPage<RepairRequest> result = repairService.page(new Page<>(current, size), wrapper);
        return Result.ok(result);
    }

    @Operation(summary = "更新报修状态")
    @PostMapping("updateStatus")
    public Result updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        RepairRequest repair = repairService.getById(id);
        if (repair == null) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR, "工单不存在");
        }
        RepairStatus newStatus = RepairStatus.values()[status];
        repair.setStatus(newStatus);
        repairService.updateById(repair);

        // 发通知给用户
        try {
            String statusName = newStatus.getName();
            rabbitTemplate.convertAndSend(
                    RabbitMQConfiguration.NOTIFICATION_EXCHANGE,
                    RabbitMQConfiguration.NOTIFICATION_ROUTING_KEY,
                    new NotificationMessage(
                            repair.getUserId(),
                            "报修状态变更",
                            "您的报修工单「" + repair.getTitle() + "」状态已变更为：" + statusName,
                            4, repair.getId()));
        } catch (Exception e) {
            log.warn("发送报修状态通知失败: {}", e.getMessage());
        }
        return Result.ok();
    }
}
