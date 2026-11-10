package com.it.xxhealth.controller;

import com.it.xxhealth.common.Result;
import com.it.xxhealth.dto.ScheduleDayDto;
import com.it.xxhealth.dto.ScheduleDto;
import com.it.xxhealth.dto.ScheduleQueryDto;
import com.it.xxhealth.service.ScheduleService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 医生端：日程管理
 */
@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    @Resource
    private ScheduleService scheduleService;

    /**
     * 按月查询预约（日历打点 + 当月列表）
     */
    @PostMapping("/listByMonth")
    public Result<List<ScheduleDto>> listByMonth(@RequestBody ScheduleQueryDto query) {
        if (query == null) {
            return Result.error(400, "参数不能为空");
        }
        return Result.success(scheduleService.listByMonth(query.getYear(), query.getMonth()));
    }

    /**
     * 某一天的日程详情（预约列表 + 检查提醒 + 异常提醒）
     */
    @PostMapping("/dayDetail")
    public Result<ScheduleDayDto> dayDetail(@RequestBody ScheduleQueryDto query) {
        if (query == null || query.getDate() == null) {
            return Result.error(400, "日期不能为空");
        }
        return Result.success(scheduleService.dayDetail(query.getDate()));
    }
}
