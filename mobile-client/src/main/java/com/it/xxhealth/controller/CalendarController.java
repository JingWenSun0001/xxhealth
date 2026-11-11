package com.it.xxhealth.controller;

import com.it.xxhealth.dto.CalendarRequestDto;
import com.it.xxhealth.dto.CalendarResponseDto;
import com.it.xxhealth.service.CalendarService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端 - 预约日历接口（按文档命名）
 */
@RestController
@RequestMapping("/calendar")
public class CalendarController {

    @Autowired
    private CalendarService calendarService;

    @PostMapping("/listAppointmentCalendar")
    public List<CalendarResponseDto> listAppointmentCalendar(@RequestBody CalendarRequestDto req) {
        if (req == null || req.getHpid() == null || req.getYear() == null || req.getMonth() == null
                || req.getMonth() < 1 || req.getMonth() > 12) {
            return new ArrayList<>();
        }
        return calendarService.list(req);
    }
}
