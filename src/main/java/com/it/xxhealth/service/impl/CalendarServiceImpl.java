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
 * 预约日历业务实现，计算每日已用量和剩余量。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
@Service
public class CalendarServiceImpl implements CalendarService {

    /**
     * 当前类使用的固定配置值。
     */
    private static final int DEFAULT_TOTAL = 20;

    /**
     * 当前模块的数据访问组件。
     */
    @Autowired
    private HospitalMapper hospitalMapper;

    /**
     * 当前模块的数据访问组件。
     */
    @Autowired
    private OrdersMapper ordersMapper;

    /**
     * 根据查询条件获取相关业务数据列表。
     * @param req 预约日历查询参数
     * @return 符合条件的数据集合
     */
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
     * @param rule rule 参数
     * @return 处理结果数值
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
