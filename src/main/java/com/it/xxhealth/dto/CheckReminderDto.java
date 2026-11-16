package com.it.xxhealth.dto;

import lombok.Data;

/**
 * 检查提醒：根据当天预约套餐里的检查项生成
 */
@Data
public class CheckReminderDto {

    private Integer orderid;

    private String patientName;

    /** 检查项名称 */
    private String itemName;

    /** 检查项内容（提醒文案） */
    private String content;

    /** 时间段 */
    private String timeSlot;
}
