package com.it.xxhealth.doctor;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 医生端到用户端的数据投影服务，将预约状态和已发布报告同步回旧业务表。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
@Service
public class DoctorMobileProjectionService {
    /**
     * 用户端订单映射到医生预约编号时使用的固定前缀。
     */
    private static final String MOBILE_PREFIX = "MOB";
    /**
     * 执行参数化 SQL 和事务内数据库操作的 JdbcTemplate。
     */
    private final JdbcTemplate db;

    /**
     * 创建 DoctorMobileProjectionService 实例并注入所需依赖。
     * @param db 数据库访问组件
     */
    public DoctorMobileProjectionService(JdbcTemplate db) {
        this.db = db;
    }

    /**
     * 同步关联业务数据并保持两端状态一致。
     * @param appointmentId 医生端预约编号
     */
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

    /**
     * 同步关联业务数据并保持两端状态一致。
     * @param reportId 体检报告编号
     */
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

    /**
     * 从用户端预约编号中解析原订单编号。
     * @param appointmentNo 医生端预约业务编号
     * @return 方法处理结果
     */
    private Integer orderId(String appointmentNo) {
        try {
            return Integer.valueOf(appointmentNo.substring(MOBILE_PREFIX.length()));
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    /**
     * 读取键值并转换为去除首尾空白的文本。
     * @param value 待转换或校验的输入值
     * @return 方法处理结果
     */
    private String text(Object value) {
        return value == null ? "" : value.toString().trim();
    }
}
