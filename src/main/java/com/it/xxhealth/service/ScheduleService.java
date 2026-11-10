package com.it.xxhealth.service;

import com.it.xxhealth.dto.ScheduleDayDto;
import com.it.xxhealth.dto.ScheduleDto;

import java.util.List;

/**
 * 医生端日程 Service
 */
public interface ScheduleService {

    /**
     * 按月查询预约（用于日历打点 + 当天列表）
     */
    List<ScheduleDto> listByMonth(Integer year, Integer month);

    /**
     * 某一天的日程详情：预约列表 + 检查提醒 + 异常提醒
     */
    ScheduleDayDto dayDetail(String date);
}
