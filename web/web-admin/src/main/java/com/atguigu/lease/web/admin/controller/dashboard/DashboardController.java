package com.atguigu.lease.web.admin.controller.dashboard;

import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.model.entity.ViewAppointment;
import com.atguigu.lease.model.enums.AppointmentStatus;
import com.atguigu.lease.web.admin.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "\u6570\u636e\u770b\u677f")
@RestController
@RequestMapping("/admin/dashboard")
@Slf4j
public class DashboardController {

    @Autowired
    private ApartmentInfoService apartmentInfoService;

    @Autowired
    private RoomInfoService roomInfoService;

    @Autowired
    private LeaseAgreementService leaseAgreementService;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private ViewAppointmentService viewAppointmentService;

    @Operation(summary = "\u83b7\u53d6\u770b\u677f\u7edf\u8ba1\u6570\u636e")
    @GetMapping("statistics")
    public Result<Map<String, Object>> statistics() {
        Map<String, Object> stats = new HashMap<>();

        // ---- \u57fa\u7840\u8ba1\u6570 ----
        long apartmentCount = apartmentInfoService.count();
        long roomCount = roomInfoService.count();
        long userCount = userInfoService.count();
        stats.put("apartmentCount", apartmentCount);
        stats.put("roomCount", roomCount);
        stats.put("userCount", userCount);

        // ---- \u79df\u7ea6\u72b6\u6001\u7edf\u8ba1\uff08\u5355\u6761SQL\u66ff\u4ee3\u5faa\u73af\u67e5\u8be2\uff09 ----
        List<Map<String, Object>> statusRows = leaseAgreementService.countLeasesByStatus();
        Map<String, Object> statusCounts = statusRows.isEmpty() ? Collections.emptyMap() : statusRows.get(0);

        String[] statusKeys = {"signingCount", "signedCount", "canceledCount", "expiredCount",
                "withdrawingCount", "withdrawnCount", "renewingCount"};
        String[] statusNames = {"\u7b7e\u7ea6\u5f85\u786e\u8ba4", "\u5df2\u7b7e\u7ea6", "\u5df2\u53d6\u6d88",
                "\u5df2\u5230\u671f", "\u9000\u79df\u5f85\u786e\u8ba4", "\u5df2\u9000\u79df", "\u7eed\u7ea6\u5f85\u786e\u8ba4"};

        Map<String, Long> leaseStatusMap = new LinkedHashMap<>();
        for (int i = 0; i < statusKeys.length; i++) {
            Object val = statusCounts.get(statusKeys[i]);
            leaseStatusMap.put(statusNames[i], val != null ? ((Number) val).longValue() : 0L);
        }
        stats.put("leaseStatusMap", leaseStatusMap);

        long signedCount = leaseStatusMap.getOrDefault("\u5df2\u7b7e\u7ea6", 0L);
        long withdrawingCount = leaseStatusMap.getOrDefault("\u9000\u79df\u5f85\u786e\u8ba4", 0L);
        long freeRoomCount = Math.max(0, roomCount - signedCount - withdrawingCount);
        stats.put("freeRoomCount", freeRoomCount);

        double occupancyRate = roomCount > 0 ? (double) (signedCount + withdrawingCount) / roomCount * 100 : 0;
        stats.put("occupancyRate", Math.round(occupancyRate * 10) / 10.0);

        // ---- \u623f\u95f4\u72b6\u6001\u5206\u5e03\uff08\u997c\u56fe\u6570\u636e\uff09 ----
        List<Map<String, Object>> roomStatusList = new ArrayList<>();
        roomStatusList.add(Map.of("name", "\u5df2\u7b7e\u7ea6", "value", signedCount));
        roomStatusList.add(Map.of("name", "\u9000\u79df\u4e2d", "value", withdrawingCount));
        roomStatusList.add(Map.of("name", "\u7a7a\u95f2", "value", freeRoomCount));
        stats.put("roomStatusList", roomStatusList);

        // ---- \u5404\u516c\u5bd3\u623f\u95f4\u6570\u91cf\uff08GROUP BY\u66ff\u4ee3N+1\u67e5\u8be2\uff09 ----
        List<Map<String, Object>> apartmentRoomList = roomInfoService.countRoomsByApartment();
        stats.put("apartmentRoomList", apartmentRoomList);

        // ---- \u6708\u5ea6\u8d8b\u52bf\uff08\u8fd112\u4e2a\u6708\u79df\u7ea6\u65b0\u589e/\u7ed3\u675f\uff09 ----
        List<Map<String, Object>> rawTrendList = leaseAgreementService.countLeasesByMonth();
        List<Map<String, Object>> monthlyTrendList = new ArrayList<>();
        for (Map<String, Object> row : rawTrendList) {
            Map<String, Object> item = new HashMap<>();
            item.put("month", row.get("month"));
            Object nc = row.get("newCount");
            Object ec = row.get("endedCount");
            item.put("newCount", nc != null ? ((Number) nc).longValue() : 0L);
            item.put("endedCount", ec != null ? ((Number) ec).longValue() : 0L);
            monthlyTrendList.add(item);
        }
        stats.put("monthlyTrendList", monthlyTrendList);

        // ---- \u9884\u7ea6\u72b6\u6001\u7edf\u8ba1 ----
        List<Map<String, Object>> appointmentStatusList = new ArrayList<>();
        for (AppointmentStatus as : AppointmentStatus.values()) {
            LambdaQueryWrapper<ViewAppointment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ViewAppointment::getAppointmentStatus, as);
            long count = viewAppointmentService.count(wrapper);
            appointmentStatusList.add(Map.of("name", as.getName(), "value", count));
        }
        stats.put("appointmentStatusList", appointmentStatusList);

        return Result.ok(stats);
    }
}
