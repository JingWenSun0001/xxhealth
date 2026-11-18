package com.it.xxhealth.dto;

import lombok.Data;

/**
 * 预约日历查询参数。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
@Data
public class CalendarRequestDto {

    /**
     * 体检机构编号。
     */
    private Integer hpid;

    /**
     * 查询年份。
     */
    private Integer year;

    /**
     * 查询月份，取值范围为 1 至 12。
     */
    private Integer month;
}
