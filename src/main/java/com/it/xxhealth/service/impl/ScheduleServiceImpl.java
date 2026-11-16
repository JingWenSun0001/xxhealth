package com.it.xxhealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.it.xxhealth.dto.AbnormalReminderDto;
import com.it.xxhealth.dto.CheckReminderDto;
import com.it.xxhealth.dto.ScheduleDayDto;
import com.it.xxhealth.dto.ScheduleDto;
import com.it.xxhealth.entity.Checkitem;
import com.it.xxhealth.entity.Cidetailedreport;
import com.it.xxhealth.entity.Orders;
import com.it.xxhealth.entity.Setmeal;
import com.it.xxhealth.entity.Setmealdetailed;
import com.it.xxhealth.entity.Users;
import com.it.xxhealth.mapper.CheckitemMapper;
import com.it.xxhealth.mapper.CidetailedreportMapper;
import com.it.xxhealth.mapper.OrdersMapper;
import com.it.xxhealth.mapper.SetmealMapper;
import com.it.xxhealth.mapper.SetmealdetailedMapper;
import com.it.xxhealth.mapper.UsersMapper;
import com.it.xxhealth.service.ScheduleService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");

    @Resource
    private OrdersMapper ordersMapper;

    @Resource
    private UsersMapper usersMapper;

    @Resource
    private SetmealMapper setmealMapper;

    @Resource
    private CidetailedreportMapper cidetailedreportMapper;

    @Resource
    private SetmealdetailedMapper setmealdetailedMapper;

    @Resource
    private CheckitemMapper checkitemMapper;

    @Override
    public List<ScheduleDto> listByMonth(Integer year, Integer month) {
        if (year == null || month == null || month < 1 || month > 12) {
            return new ArrayList<>();
        }
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        List<Orders> orders = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .ge(Orders::getOrderdate, java.sql.Date.valueOf(start))
                .le(Orders::getOrderdate, java.sql.Date.valueOf(end))
                .orderByAsc(Orders::getOrderdate)
                .orderByAsc(Orders::getOrderid));
        return buildSchedule(orders);
    }

    @Override
    public ScheduleDayDto dayDetail(String date) {
        ScheduleDayDto result = new ScheduleDayDto();
        result.setDate(date);
        if (date == null || date.trim().isEmpty()) {
            return result;
        }
        LocalDate localDate;
        try {
            localDate = LocalDate.parse(date.trim());
        } catch (Exception e) {
            return result;
        }

        List<Orders> orders = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getOrderdate, java.sql.Date.valueOf(localDate))
                .orderByAsc(Orders::getOrderid));
        List<ScheduleDto> appointments = buildSchedule(orders);
        result.setAppointments(appointments);
        if (appointments.isEmpty()) {
            return result;
        }

        Map<Integer, ScheduleDto> byOrder = appointments.stream()
                .collect(Collectors.toMap(ScheduleDto::getOrderid, a -> a, (a, b) -> a));
        List<Integer> orderIds = appointments.stream()
                .map(ScheduleDto::getOrderid)
                .collect(Collectors.toList());

        // 异常提醒：当天预约对应的异常明细
        List<Cidetailedreport> badList = cidetailedreportMapper.selectList(
                new LambdaQueryWrapper<Cidetailedreport>()
                        .in(Cidetailedreport::getOrderid, orderIds)
                        .eq(Cidetailedreport::getIserror, 1)
                        .orderByAsc(Cidetailedreport::getOrderid)
                        .orderByAsc(Cidetailedreport::getCidrid));
        List<AbnormalReminderDto> abnormalList = new ArrayList<>();
        for (Cidetailedreport detail : badList) {
            ScheduleDto appt = byOrder.get(detail.getOrderid());
            if (appt == null) {
                continue;
            }
            AbnormalReminderDto item = new AbnormalReminderDto();
            item.setOrderid(detail.getOrderid());
            item.setPatientName(appt.getPatientName());
            item.setItem(detail.getName());
            item.setValue(detail.getValue());
            item.setUnit(detail.getUnit());
            item.setRangeText(rangeText(detail));
            item.setLevel(levelOf(detail));
            item.setTimeSlot(appt.getTimeSlot());
            abnormalList.add(item);
        }
        result.setAbnormalReminders(abnormalList);

        // 检查提醒：套餐内前 3 个检查项
        List<CheckReminderDto> checkList = new ArrayList<>();
        for (Orders order : orders) {
            ScheduleDto appt = byOrder.get(order.getOrderid());
            if (appt == null || order.getSmid() == null) {
                continue;
            }
            List<Setmealdetailed> items = setmealdetailedMapper.selectList(
                    new LambdaQueryWrapper<Setmealdetailed>()
                            .eq(Setmealdetailed::getSmid, order.getSmid())
                            .orderByAsc(Setmealdetailed::getSdid));
            int limit = Math.min(3, items.size());
            for (int i = 0; i < limit; i++) {
                Checkitem checkitem = checkitemMapper.selectById(items.get(i).getCiid());
                if (checkitem == null) {
                    continue;
                }
                CheckReminderDto reminder = new CheckReminderDto();
                reminder.setOrderid(order.getOrderid());
                reminder.setPatientName(appt.getPatientName());
                reminder.setItemName(checkitem.getCiname());
                reminder.setContent(checkitem.getCicontent());
                reminder.setTimeSlot(appt.getTimeSlot());
                checkList.add(reminder);
            }
        }
        result.setCheckReminders(checkList);
        return result;
    }

    private List<ScheduleDto> buildSchedule(List<Orders> orders) {
        List<ScheduleDto> list = new ArrayList<>();
        if (orders == null || orders.isEmpty()) {
            return list;
        }

        Set<String> userIds = new HashSet<>();
        Set<Integer> smIds = new HashSet<>();
        for (Orders order : orders) {
            if (order.getUserid() != null) {
                userIds.add(order.getUserid());
            }
            if (order.getSmid() != null) {
                smIds.add(order.getSmid());
            }
        }

        Map<String, Users> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (Users user : usersMapper.selectBatchIds(userIds)) {
                userMap.put(user.getUserid(), user);
            }
        }
        Map<Integer, Setmeal> setmealMap = new HashMap<>();
        if (!smIds.isEmpty()) {
            for (Setmeal setmeal : setmealMapper.selectBatchIds(smIds)) {
                setmealMap.put(setmeal.getSmid(), setmeal);
            }
        }

        List<Integer> orderIds = orders.stream().map(Orders::getOrderid).collect(Collectors.toList());
        Map<Integer, Long> abnormalCountMap = new HashMap<>();
        List<Cidetailedreport> badList = cidetailedreportMapper.selectList(
                new LambdaQueryWrapper<Cidetailedreport>()
                        .in(Cidetailedreport::getOrderid, orderIds)
                        .eq(Cidetailedreport::getIserror, 1));
        for (Cidetailedreport detail : badList) {
            abnormalCountMap.merge(detail.getOrderid(), 1L, Long::sum);
        }

        Map<String, Integer> daySequence = new HashMap<>();
        for (Orders order : orders) {
            String date = DATE_FMT.format(order.getOrderdate());
            int index = daySequence.getOrDefault(date, 0);
            daySequence.put(date, index + 1);

            ScheduleDto dto = new ScheduleDto();
            dto.setOrderid(order.getOrderid());
            dto.setDate(date);
            LocalTime start = LocalTime.of(8, 0).plusMinutes(30L * index);
            dto.setTimeSlot(start.format(TIME_FMT) + "-" + start.plusMinutes(30).format(TIME_FMT));

            Users user = userMap.get(order.getUserid());
            if (user != null) {
                dto.setPatientName(user.getRealname());
                dto.setSex(user.getSex());
                dto.setAge(ageOf(user));
            }
            Setmeal setmeal = order.getSmid() == null ? null : setmealMap.get(order.getSmid());
            dto.setSetmealName(setmeal == null ? "—" : setmeal.getName());

            dto.setState(order.getState());
            dto.setStateText(order.getState() != null && order.getState() == 2 ? "已完成" : "已预约");
            dto.setAbnormalCount(abnormalCountMap.getOrDefault(order.getOrderid(), 0L).intValue());
            list.add(dto);
        }
        return list;
    }

    private Integer ageOf(Users user) {
        if (user == null || user.getBirthday() == null) {
            return null;
        }
        LocalDate birthday = new java.sql.Date(user.getBirthday().getTime()).toLocalDate();
        return Period.between(birthday, LocalDate.now()).getYears();
    }

    private String rangeText(Cidetailedreport detail) {
        if (detail.getNormalvaluestring() != null && !detail.getNormalvaluestring().trim().isEmpty()) {
            return detail.getNormalvaluestring();
        }
        if (detail.getMinrange() != null && detail.getMaxrange() != null) {
            return detail.getMinrange() + " ~ " + detail.getMaxrange();
        }
        if (detail.getNormalvalue() != null) {
            return detail.getNormalvalue();
        }
        return "—";
    }

    private String levelOf(Cidetailedreport detail) {
        if (detail.getValue() == null) {
            return "异常";
        }
        try {
            double value = Double.parseDouble(detail.getValue().trim());
            if (detail.getMaxrange() != null && value > detail.getMaxrange()) {
                return "偏高";
            }
            if (detail.getMinrange() != null && value < detail.getMinrange()) {
                return "偏低";
            }
        } catch (Exception ignored) {
            // 非数值结果（如血压 118/76）统一标记为异常
        }
        return "异常";
    }
}
