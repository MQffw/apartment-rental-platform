package com.atguigu.lease.web.app.controller.history;


import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.model.entity.BrowsingHistory;
import com.atguigu.lease.web.app.custom.LoginUser;
import com.atguigu.lease.web.app.custom.holder.LoginUserHolder;
import com.atguigu.lease.web.app.service.BrowsingHistoryService;
import com.atguigu.lease.web.app.vo.history.HistoryItemVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@RestController
@Tag(name = "浏览历史管理")
@RequestMapping("/app/history")
@Slf4j
public class BrowsingHistoryController {

    @Autowired
    private BrowsingHistoryService service;

    private Long getCurrentUserId() {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_AUTH);
        }
        return loginUser.getUserId();
    }

    @Operation(summary = "获取浏览历史")
    @GetMapping("pageItem")
    public Result<IPage<HistoryItemVo>> page(@RequestParam long current, @RequestParam long size) {
        Page<HistoryItemVo> page = new Page<>(current, size);
        Long userId = getCurrentUserId();
        IPage<HistoryItemVo> result = service.pageItemByUserId(page, userId);
        return Result.ok(result);
    }

    @Operation(summary = "删除单条浏览历史")
    @DeleteMapping("deleteById")
    public Result deleteById(@RequestParam Long id) {
        if (id == null || id <= 0) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR, "记录ID不能为空");
        }
        Long userId = getCurrentUserId();
        // 校验归属
        BrowsingHistory history = service.getById(id);
        if (history == null || !history.getUserId().equals(userId)) {
            throw new LeaseException(ResultCodeEnum.ILLEGAL_REQUEST);
        }
        service.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "清空浏览历史")
    @DeleteMapping("clearAll")
    public Result clearAll() {
        Long userId = getCurrentUserId();
        LambdaQueryWrapper<BrowsingHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BrowsingHistory::getUserId, userId);
        service.remove(queryWrapper);
        return Result.ok();
    }
}
