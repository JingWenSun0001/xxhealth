package com.it.xxhealth.dto;

import lombok.Data;

/**
 * 预约日历单日数据
 */
@Data
public class CalendarResponseDto {

    /** yyyy-MM-dd */
    private String date;

    /** 当天名额上限 */
    private Integer total;

    /** 已预约人数 */
    private Integer used;

    /** 剩余名额 */
    private Integer remaining;

    /** 是否已约满 */
    private Boolean full;
}
