package com.it.xxhealth.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * 某一天的日程详情：预约列表 + 检查提醒 + 异常提醒
 */
@Data
public class ScheduleDayDto {

    private String date;

    private List<ScheduleDto> appointments = new ArrayList<>();

    private List<CheckReminderDto> checkReminders = new ArrayList<>();

    private List<AbnormalReminderDto> abnormalReminders = new ArrayList<>();
}
