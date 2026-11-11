package com.it.xxhealth.dto;

import lombok.Data;

/**
 * 预约日历请求
 */
@Data
public class CalendarRequestDto {

    private Integer hpid;

    private Integer year;

    private Integer month;
}
