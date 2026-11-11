package com.it.xxhealth.doctor;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Projects doctor-side workflow changes back to the legacy mobile order model. */
@Service
public class DoctorMobileProjectionService {
    private static final String MOBILE_PREFIX = "MOB";
    private final JdbcTemplate db;

    public DoctorMobileProjectionService(JdbcTemplate db) {
        this.db = db;
    }

    @Transactional
    public void syncAppointment(long appointmentId) {
        List<Map<String, Object>> rows = db.queryForList(
                "SELECT appointment_no,exam_date,status,source FROM doctor_appointment WHERE id=?",
                appointmentId);
        if (rows.isEmpty()) {
            return;
        }
        Map<String, Object> appointment = rows.get(0);
        String appointmentNo = text(appointment.get("appointment_no"));
        if (!"手机端".equals(text(appointment.get("source"))) || !appointmentNo.startsWith(MOBILE_PREFIX)) {
            return;
        }
        Integer orderId = orderId(appointmentNo);
        if (orderId == null) {
            return;
        }
        String status = text(appointment.get("status"));
        int state = "已完成".equals(status) ? 2 : "已取消".equals(status) ? 3 : 1;
        db.update("UPDATE orders SET orderDate=?,state=? WHERE orderId=?",
                appointment.get("exam_date"), state, orderId);
    }

    @Transactional
    public void syncPublishedReport(long reportId) {
        List<Map<String, Object>> rows = db.queryForList(
                "SELECT p.phone,r.exam_date,r.package_name FROM doctor_report r "
                        + "JOIN doctor_patient p ON p.id=r.patient_id WHERE r.id=? AND r.status='已生成'",
                reportId);
        if (rows.isEmpty()) {
            return;
        }
        Map<String, Object> report = rows.get(0);
        db.update("UPDATE orders o JOIN setmeal s ON s.smId=o.smId SET o.state=2 "
                        + "WHERE o.userId=? AND o.orderDate=? AND s.name=?",
                report.get("phone"), report.get("exam_date"), report.get("package_name"));
    }

    private Integer orderId(String appointmentNo) {
        try {
            return Integer.valueOf(appointmentNo.substring(MOBILE_PREFIX.length()));
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private String text(Object value) {
        return value == null ? "" : value.toString().trim();
    }
}
