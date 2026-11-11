package com.it.xxhealth.doctor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.it.xxhealth.entity.Orders;
import com.it.xxhealth.entity.Users;
import com.it.xxhealth.service.OrdersService;
import com.it.xxhealth.service.UsersService;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MobileWorkspaceIntegrationTests {
    @Autowired private UsersService usersService;
    @Autowired private OrdersService ordersService;
    @Autowired private DoctorWorkspaceService doctorWorkspace;
    @Autowired private DoctorMobileProjectionService projection;
    @Autowired private MobileWorkspaceService mobileWorkspace;
    @Autowired private JdbcTemplate db;

    @Test
    void doctorStatusAndPublishedReportAreVisibleOnMobile() {
        String suffix = String.valueOf(Math.abs(UUID.randomUUID().hashCode()));
        suffix = ("000000000" + suffix).substring(("000000000" + suffix).length() - 9);
        String phone = "18" + suffix;
        String identity = "21010219900101" + suffix.substring(0, 3) + "X";

        Users user = new Users();
        user.setUserid(phone);
        user.setPassword("123456");
        user.setRealname("双向联调患者");
        user.setSex(1);
        user.setIdentitycard(identity);
        user.setBirthday(Date.valueOf("1990-01-01"));
        user.setUsertype(1);
        assertEquals(1, usersService.saveUsers(user));

        Integer hospitalId = db.queryForObject("SELECT hpId FROM hospital ORDER BY hpId LIMIT 1", Integer.class);
        Integer setmealId = db.queryForObject("SELECT smId FROM setmeal ORDER BY smId LIMIT 1", Integer.class);
        String packageName = db.queryForObject("SELECT name FROM setmeal WHERE smId=?", String.class, setmealId);
        Orders order = new Orders();
        order.setUserid(phone);
        order.setHpid(hospitalId);
        order.setSmid(setmealId);
        order.setOrderdate(Date.valueOf(LocalDate.now().plusDays(1)));
        assertEquals(1, ordersService.saveOrder(order));

        Long appointmentId = db.queryForObject(
                "SELECT id FROM doctor_appointment WHERE appointment_no=?", Long.class,
                "MOB" + String.format("%010d", order.getOrderid()));
        doctorWorkspace.appointmentStatus(appointmentId, Collections.singletonMap("status", "已确认"));
        List<Map<String, Object>> mobileAppointments = mobileWorkspace.appointments(phone);
        assertEquals("已确认", mobileAppointments.get(0).get("doctorStatus"));

        doctorWorkspace.appointmentStatus(appointmentId, Collections.singletonMap("status", "已完成"));
        assertEquals(2, db.queryForObject("SELECT state FROM orders WHERE orderId=?", Integer.class, order.getOrderid()));
        assertEquals("已完成", mobileWorkspace.appointments(phone).get(0).get("doctorStatus"));

        Long patientId = db.queryForObject("SELECT patient_id FROM doctor_appointment WHERE id=?", Long.class, appointmentId);
        db.update("INSERT INTO doctor_report(patient_id,report_no,exam_date,package_name,status,score,conclusion,advice,items) "
                        + "VALUES(?,?,?,?,'已生成',96,'状态良好','保持规律作息',?)",
                patientId, "TEST" + UUID.randomUUID().toString().replace("-", ""),
                order.getOrderdate(), packageName,
                "[{\"group\":\"基础项目\",\"name\":\"血压\",\"value\":\"120/80\",\"unit\":\"mmHg\",\"range\":\"90-140/60-90\",\"abnormal\":false}]");
        Long reportId = db.queryForObject("SELECT id FROM doctor_report WHERE patient_id=? ORDER BY id DESC LIMIT 1", Long.class, patientId);
        projection.syncPublishedReport(reportId);

        assertFalse(mobileWorkspace.reports(phone).isEmpty());
        assertEquals("状态良好", mobileWorkspace.report(phone, reportId).get("conclusion"));
        assertEquals(1, ((List<?>) mobileWorkspace.report(phone, reportId).get("items")).size());
    }

    @Test
    void doctorPackagesCanBeBookedFromMobile() {
        String suffix = String.valueOf(Math.abs(UUID.randomUUID().hashCode()));
        suffix = ("000000000" + suffix).substring(("000000000" + suffix).length() - 9);
        String phone = "18" + suffix;
        Users user = new Users();
        user.setUserid(phone);
        user.setPassword("123456");
        user.setRealname("套餐联调患者");
        user.setSex(1);
        user.setIdentitycard("21010219900101" + suffix.substring(0, 3) + "X");
        user.setBirthday(Date.valueOf("1990-01-01"));
        user.setUsertype(1);
        assertEquals(1, usersService.saveUsers(user));

        List<Map<String, Object>> packages = mobileWorkspace.packages();
        assertFalse(packages.isEmpty());
        Map<String, Object> selected = packages.stream()
                .filter(item -> "全部".equals(item.get("gender")) || "男".equals(item.get("gender")))
                .findFirst().orElseThrow(AssertionError::new);
        Integer hospitalId = db.queryForObject("SELECT hpId FROM hospital WHERE state=1 ORDER BY hpId LIMIT 1", Integer.class);
        Map<String, Object> result = mobileWorkspace.book(phone,
                ((Number) selected.get("packageId")).longValue(), hospitalId,
                LocalDate.now().plusDays(2).toString());

        assertEquals(1, result.get("result"));
        assertEquals(selected.get("name"), db.queryForObject(
                "SELECT package_name FROM doctor_appointment WHERE appointment_no=CONCAT('MOB',LPAD(?,10,'0'))",
                String.class, result.get("orderId")));
    }
}
