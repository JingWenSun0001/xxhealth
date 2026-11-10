package com.it.xxhealth.service.impl;

import com.it.xxhealth.dto.CalendarRequestDto;
import com.it.xxhealth.dto.CalendarResponseDto;
import com.it.xxhealth.entity.Hospital;
import com.it.xxhealth.mapper.HospitalMapper;
import com.it.xxhealth.mapper.OrdersMapper;
import com.it.xxhealth.service.CalendarService;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 预约日历 Service 实现
 */
@Service
public class CalendarServiceImpl implements CalendarService {

    private static final int DEFAULT_TOTAL = 20;

    @Autowired
    private HospitalMapper hospitalMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Override
    public List<CalendarResponseDto> list(CalendarRequestDto req) {
        Hospital hospital = hospitalMapper.selectById(req.getHpid());
        int total = parseTotal(hospital == null ? null : hospital.getRule());

        Map<Integer, Integer> usedMap = new HashMap<>();
        List<Map<String, Object>> rows = ordersMapper.countByMonth(req.getHpid(), req.getYear(), req.getMonth());
        for (Map<String, Object> row : rows) {
            Object day = row.get("day");
            Object cnt = row.get("cnt");
            if (day instanceof Number && cnt instanceof Number) {
                usedMap.put(((Number) day).intValue(), ((Number) cnt).intValue());
            }
        }

        YearMonth ym = YearMonth.of(req.getYear(), req.getMonth());
        List<CalendarResponseDto> list = new ArrayList<>();
        for (int day = 1; day <= ym.lengthOfMonth(); day++) {
            int used = usedMap.getOrDefault(day, 0);
            int remaining = Math.max(total - used, 0);

            CalendarResponseDto dto = new CalendarResponseDto();
            dto.setDate(ym.atDay(day).toString());
            dto.setTotal(total);
            dto.setUsed(used);
            dto.setRemaining(remaining);
            dto.setFull(remaining <= 0);
            list.add(dto);
        }
        return list;
    }

    /**
     * 从 "每天限20人" 这类文本里解析名额，解析失败默认 20
     */
    private int parseTotal(String rule) {
        if (rule != null) {
            Matcher matcher = Pattern.compile("(\\d+)").matcher(rule);
            if (matcher.find()) {
                try {
                    return Integer.parseInt(matcher.group(1));
                } catch (NumberFormatException ignored) {
                    // 落到默认值
                }
            }
        }
        return DEFAULT_TOTAL;
    }
}
