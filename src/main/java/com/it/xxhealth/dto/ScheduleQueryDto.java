package com.it.xxhealth.dto;

import lombok.Data;

/**
 * 日程查询参数：按月查或按天查
 */
@Data
public class ScheduleQueryDto {

    /** 年（按月查询时使用） */
    private Integer year;

    /** 月 1-12（按月查询时使用） */
    private Integer month;

    /** 日期 yyyy-MM-dd（按天查询时使用） */
    private String date;
}
