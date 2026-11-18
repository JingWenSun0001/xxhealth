package com.it.xxhealth.dto;

import lombok.Data;

/**
 * 预约日历单日容量响应。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
@Data
public class CalendarResponseDto {

    /**
     * yyyy-MM-dd
     */
    private String date;

    /**
     * 当天名额上限
     */
    private Integer total;

    /**
     * 已预约人数
     */
    private Integer used;

    /**
     * 剩余名额
     */
    private Integer remaining;

    /**
     * 是否已约满
     */
    private Boolean full;
}
