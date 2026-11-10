package com.it.xxhealth.service;

import com.it.xxhealth.dto.CalendarRequestDto;
import com.it.xxhealth.dto.CalendarResponseDto;
import java.util.List;

/**
 * 预约日历 Service
 */
public interface CalendarService {

    /** 生成某医院某月的预约日历 */
    List<CalendarResponseDto> list(CalendarRequestDto req);
}
