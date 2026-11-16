package com.it.xxhealth.dto;

import lombok.Data;

/**
 * 日程（预约）列表项：医生端按日期查看当天受检者
 */
@Data
public class ScheduleDto {

    /** 预约编号 */
    private Integer orderid;

    /** 预约日期 yyyy-MM-dd */
    private String date;

    /** 时间段，如 08:00-08:30（orders 暂无时间段字段，按当天顺序生成） */
    private String timeSlot;

    /** 患者姓名 */
    private String patientName;

    /** 性别 1 男 / 0 女 */
    private Integer sex;

    /** 年龄 */
    private Integer age;

    /** 体检套餐名称 */
    private String setmealName;

    /** 订单状态 1 未归档 / 2 已归档 */
    private Integer state;

    /** 状态文案 */
    private String stateText;

    /** 异常项数量 */
    private Integer abnormalCount;
}
