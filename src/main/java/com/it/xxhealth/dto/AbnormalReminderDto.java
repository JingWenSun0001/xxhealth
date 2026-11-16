package com.it.xxhealth.dto;

import lombok.Data;

/**
 * 异常提醒：某天预约对应的体检异常指标
 */
@Data
public class AbnormalReminderDto {

    private Integer orderid;

    private String patientName;

    private String item;

    private String value;

    private String unit;

    /** 偏高 / 偏低 / 异常 */
    private String level;

    /** 参考范围文案 */
    private String rangeText;

    /** 时间段 */
    private String timeSlot;
}
