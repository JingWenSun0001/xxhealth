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
 * 预约日历接口，按体检机构和月份返回每日剩余容量。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
@RestController
@RequestMapping("/calendar")
public class CalendarController {

    /**
     * 预约日历业务服务。
     */
    @Autowired
    private CalendarService calendarService;

    /**
     * 查询指定机构和月份的每日预约容量。
     * @param req 预约日历查询参数
     * @return 符合条件的数据集合
     */
    @PostMapping("/listAppointmentCalendar")
    public List<CalendarResponseDto> listAppointmentCalendar(@RequestBody CalendarRequestDto req) {
        if (req == null || req.getHpid() == null || req.getYear() == null || req.getMonth() == null
                || req.getMonth() < 1 || req.getMonth() > 12) {
            return new ArrayList<>();
        }
        return calendarService.list(req);
    }
}
