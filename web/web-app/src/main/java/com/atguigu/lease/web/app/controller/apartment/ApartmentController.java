package com.atguigu.lease.web.app.controller.apartment;

import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.model.entity.ApartmentInfo;
import com.atguigu.lease.model.enums.ReleaseStatus;
import com.atguigu.lease.web.app.service.ApartmentInfoService;
import com.atguigu.lease.web.app.vo.apartment.ApartmentDetailVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

@RestController
@Tag(name = "公寓信息")
@RequestMapping("/app/apartment")
@Slf4j
public class ApartmentController {
    @Autowired
    private ApartmentInfoService service;

    @Operation(summary = "根据id获取公寓信息")
    @GetMapping("getDetailById")
    public Result<ApartmentDetailVo> getDetailById(@RequestParam Long id) {
        ApartmentDetailVo apartmentDetailVo = service.getApartmentDetailById(id);
        return Result.ok(apartmentDetailVo);
    }

    @Operation(summary = "分页搜索公寓")
    @GetMapping("pageItem")
    public Result<Page<ApartmentInfo>> pageItem(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<ApartmentInfo> page = new Page<>(current, size);
        LambdaQueryWrapper<ApartmentInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApartmentInfo::getIsRelease, ReleaseStatus.RELEASED);
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(ApartmentInfo::getName, keyword.trim());
        }
        wrapper.orderByDesc(ApartmentInfo::getCreateTime);
        service.page(page, wrapper);
        return Result.ok(page);
    }
}
