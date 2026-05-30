package com.atguigu.lease.web.app.controller.appointment;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.web.app.custom.LoginUser;
import com.atguigu.lease.web.app.custom.holder.LoginUserHolder;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.model.entity.ViewAppointment;
import com.atguigu.lease.web.app.service.ViewAppointmentService;
import com.atguigu.lease.web.app.vo.appointment.AppointmentDetailVo;
import com.atguigu.lease.web.app.vo.appointment.AppointmentItemVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "看房预约信息")
@RestController
@RequestMapping("/app/appointment")
public class ViewAppointmentController {

    @Autowired
    private ViewAppointmentService service;

    private LoginUser getCurrentUser() {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_AUTH);
        }
        return loginUser;
    }

    @Operation(summary = "保存或更新看房预约")
    @PostMapping("/saveOrUpdate")
    public Result<Object> saveOrUpdate(@RequestBody ViewAppointment viewAppointment) {
        LoginUser loginUser = getCurrentUser();
        viewAppointment.setUserId(loginUser.getUserId());
        service.saveOrUpdate(viewAppointment);
        return Result.ok();
    }

    @Operation(summary = "查询个人预约看房列表")
    @GetMapping("listItem")
    public Result<List<AppointmentItemVo>> listItem() {
        LoginUser loginUser = getCurrentUser();
        List<AppointmentItemVo> list = service.listItemByUserId(loginUser.getUserId());
        return Result.ok(list);
    }

    @GetMapping("getDetailById")
    @Operation(summary = "根据ID查询预约详情信息")
    public Result<AppointmentDetailVo> getDetailById(@RequestParam Long id) {
        if (id == null || id <= 0) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR, "预约ID不能为空");
        }
        LoginUser loginUser = getCurrentUser();
        AppointmentDetailVo appointmentDetailVo = service.getDetailById(id, loginUser.getUserId());
        if (appointmentDetailVo == null) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR, "预约记录不存在");
        }
        return Result.ok(appointmentDetailVo);
    }
}