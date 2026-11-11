package com.it.xxhealth.doctor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Keeps the legacy mobile tables (users/orders) and the doctor workspace tables
 * in one business flow. Mobile writes remain the source of truth for patient
 * identity and mobile appointment state.
 */
@Service
public class MobileDoctorSyncService {
    private static final String MOBILE_APPOINTMENT_PREFIX = "MOB";

    private final JdbcTemplate db;
    private final ObjectMapper json;
    private final DoctorOperationsService operations;

    public MobileDoctorSyncService(JdbcTemplate db, ObjectMapper json, DoctorOperationsService operations) {
        this.db = db;
        this.json = json;
        this.operations = operations;
    }

    @Transactional
    public long syncUser(String userId) {
        Map<String, Object> user = requiredRow(
                "SELECT userId user_id,realName real_name,sex,identityCard identity_card,birthday "
                        + "FROM users WHERE userId=?",
                userId);
        String identityCard = text(user.get("identity_card"));
        String phone = text(user.get("user_id"));
        List<Long> matches = db.query(
                "SELECT id FROM doctor_patient WHERE identity_card=? OR phone=? ORDER BY deleted,id LIMIT 1",
                (rs, rowNum) -> rs.getLong(1), identityCard, phone);
        String sex = number(user.get("sex")) == 1 ? "男" : "女";
        LocalDate birthday = ((Date) user.get("birthday")).toLocalDate();
        int age = Math.max(0, Period.between(birthday, LocalDate.now()).getYears());
        if (matches.isEmpty()) {
            db.update("INSERT INTO doctor_patient(name,sex,age,phone,identity_card,health_status,tags,address,notes,avatar) "
                            + "VALUES(?,?,?,?,?,'正常','无','','手机端注册',?)",
                    text(user.get("real_name")), sex, age, phone, identityCard,
                    "男".equals(sex) ? "male" : "female");
            return db.queryForObject("SELECT id FROM doctor_patient WHERE identity_card=? ORDER BY id DESC LIMIT 1",
                    Long.class, identityCard);
        }
        long patientId = matches.get(0);
        db.update("UPDATE doctor_patient SET name=?,sex=?,age=?,phone=?,identity_card=?,avatar=?,deleted=0 WHERE id=?",
                text(user.get("real_name")), sex, age, phone, identityCard,
                "男".equals(sex) ? "male" : "female", patientId);
        return patientId;
    }

    @Transactional
    public long syncOrder(Integer orderId) {
        Map<String, Object> order = requiredRow(
                "SELECT o.orderId order_id,o.orderDate order_date,o.userId user_id,o.state,s.name package_name "
                        + "FROM orders o JOIN setmeal s ON s.smId=o.smId WHERE o.orderId=?",
                orderId);
        long patientId = syncUser(text(order.get("user_id")));
        String appointmentNo = appointmentNo(orderId);
        int orderState = number(order.get("state"));
        String status = orderState == 2 ? "已完成" : orderState == 3 ? "已取消" : "待确认";
        String examDate = order.get("order_date").toString();
        List<Map<String, Object>> existing = db.queryForList(
                "SELECT id,status,time_slot FROM doctor_appointment WHERE appointment_no=?", appointmentNo);
        long appointmentId;
        if (existing.isEmpty()) {
            String timeSlot = firstAvailableSlot(examDate);
            db.update("INSERT INTO doctor_appointment(patient_id,appointment_no,exam_date,time_slot,package_name,source,status) "
                            + "VALUES(?,?,?,?,?,'手机端',?)",
                    patientId, appointmentNo, examDate, timeSlot, text(order.get("package_name")), status);
            appointmentId = db.queryForObject(
                    "SELECT id FROM doctor_appointment WHERE appointment_no=?", Long.class, appointmentNo);
            operations.attachSchedule(appointmentId);
            operations.syncAppointmentStatus(appointmentId, status);
            operations.appointmentNotification(appointmentId, "手机端新预约");
        } else {
            Map<String, Object> current = existing.get(0);
            appointmentId = ((Number) current.get("id")).longValue();
            String currentStatus = text(current.get("status"));
            String nextStatus = "已完成".equals(status) || "已取消".equals(status) ? status : currentStatus;
            db.update("UPDATE doctor_appointment SET patient_id=?,exam_date=?,package_name=?,source='手机端',status=? WHERE id=?",
                    patientId, examDate, text(order.get("package_name")), nextStatus, appointmentId);
            db.update("UPDATE doctor_schedule SET schedule_date=?,status=? WHERE appointment_id=?",
                    examDate, scheduleStatus(nextStatus), appointmentId);
        }
        return appointmentId;
    }

    @Transactional
    public int syncAllOrders() {
        List<Integer> ids = db.query("SELECT orderId FROM orders ORDER BY orderId",
                (rs, rowNum) -> rs.getInt(1));
        for (Integer id : ids) {
            syncOrder(id);
        }
        return ids.size();
    }

    @Transactional
    public void cancelOrder(Integer orderId) {
        List<Long> ids = db.query("SELECT id FROM doctor_appointment WHERE appointment_no=?",
                (rs, rowNum) -> rs.getLong(1), appointmentNo(orderId));
        if (ids.isEmpty()) {
            return;
        }
        long appointmentId = ids.get(0);
        db.update("UPDATE doctor_appointment SET status='已取消' WHERE id=?", appointmentId);
        operations.syncAppointmentStatus(appointmentId, "已取消");
        operations.appointmentNotification(appointmentId, "手机端取消预约");
    }

    private String firstAvailableSlot(String examDate) {
        Map<String, Object> settings = requiredRow(
                "SELECT preferences,rules FROM doctor_settings WHERE id=1");
        Map<String, Object> preferences = decode(settings.get("preferences"));
        Map<String, Object> rules = decode(settings.get("rules"));
        String room = text(preferences.getOrDefault("defaultRoom", "体检一室"));
        int minutes = number(rules.getOrDefault("slotMinutes", 30));
        List<String> slots = new ArrayList<>();
        addSlots(slots, text(rules.getOrDefault("morningStart", "08:00")),
                text(rules.getOrDefault("morningEnd", "12:00")), minutes);
        addSlots(slots, text(rules.getOrDefault("afternoonStart", "13:30")),
                text(rules.getOrDefault("afternoonEnd", "17:00")), minutes);
        for (String slot : slots) {
            String[] times = slot.split("–");
            Long occupied = db.queryForObject(
                    "SELECT COUNT(*) FROM doctor_schedule WHERE schedule_date=? AND room=? "
                            + "AND start_time<? AND end_time>? AND status<>'已取消'",
                    Long.class, examDate, room, times[1], times[0]);
            if (occupied != null && occupied == 0) {
                return slot;
            }
        }
        return slots.isEmpty() ? "08:00–08:30" : slots.get(0);
    }

    private void addSlots(List<String> slots, String startText, String endText, int minutes) {
        LocalTime cursor = LocalTime.parse(startText);
        LocalTime end = LocalTime.parse(endText);
        while (!cursor.plusMinutes(minutes).isAfter(end)) {
            LocalTime next = cursor.plusMinutes(minutes);
            slots.add(cursor + "–" + next);
            cursor = next;
        }
    }

    private Map<String, Object> requiredRow(String sql, Object... args) {
        List<Map<String, Object>> rows = db.queryForList(sql, args);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("手机端数据不存在，无法同步到医生端");
        }
        return rows.get(0);
    }

    private Map<String, Object> decode(Object value) {
        try {
            return json.readValue(text(value), new TypeReference<Map<String, Object>>() { });
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private String appointmentNo(Integer orderId) {
        return MOBILE_APPOINTMENT_PREFIX + String.format("%010d", orderId);
    }

    private String scheduleStatus(String appointmentStatus) {
        if ("已完成".equals(appointmentStatus)) {
            return "已完成";
        }
        if ("已取消".equals(appointmentStatus)) {
            return "已取消";
        }
        return "已预约";
    }

    private int number(Object value) {
        return Integer.parseInt(String.valueOf(value));
    }

    private String text(Object value) {
        return value == null ? "" : value.toString().trim();
    }
}
