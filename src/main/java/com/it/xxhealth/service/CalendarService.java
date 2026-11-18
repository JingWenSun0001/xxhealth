package com.it.xxhealth.service;

import com.it.xxhealth.dto.CalendarRequestDto;
import com.it.xxhealth.dto.CalendarResponseDto;
import java.util.List;

/**
 * 预约日历业务接口。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
public interface CalendarService {

    /**
     * 生成某医院某月的预约日历
     * @param req 预约日历查询参数
     * @return 符合条件的数据集合
     */
    List<CalendarResponseDto> list(CalendarRequestDto req);
}
