package com.it.xxhealth.doctor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.it.xxhealth.entity.Orders;
import com.it.xxhealth.entity.Users;
import com.it.xxhealth.service.OrdersService;
import com.it.xxhealth.service.UsersService;
import java.sql.Date;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MobileDoctorSyncIntegrationTests {
    @Autowired private UsersService usersService;
    @Autowired private OrdersService ordersService;
    @Autowired private JdbcTemplate db;

    @Test
    void mobileRegistrationAndAppointmentReachDoctorWorkspace() {
        String suffix = String.valueOf(Math.abs(UUID.randomUUID().hashCode()));
        suffix = ("000000000" + suffix).substring(("000000000" + suffix).length() - 9);
        String phone = "18" + suffix;
        String identity = "21010219900101" + suffix.substring(0, 3) + "X";

        Users user = new Users();
        user.setUserid(phone);
        user.setPassword("123456");
        user.setRealname("联调患者");
        user.setSex(1);
        user.setIdentitycard(identity);
        user.setBirthday(Date.valueOf("1990-01-01"));
        user.setUsertype(1);
        assertEquals(1, usersService.saveUsers(user));
        assertEquals(1L, db.queryForObject(
                "SELECT COUNT(*) FROM doctor_patient WHERE phone=? AND deleted=0", Long.class, phone));

        Integer hospitalId = db.queryForObject("SELECT hpId FROM hospital ORDER BY hpId LIMIT 1", Integer.class);
        Integer setmealId = db.queryForObject("SELECT smId FROM setmeal ORDER BY smId LIMIT 1", Integer.class);
        assertNotNull(hospitalId);
        assertNotNull(setmealId);

        Orders order = new Orders();
        order.setUserid(phone);
        order.setHpid(hospitalId);
        order.setSmid(setmealId);
        order.setOrderdate(Date.valueOf(LocalDate.now().plusDays(1)));
        assertEquals(1, ordersService.saveOrder(order));

        String appointmentNo = "MOB" + String.format("%010d", order.getOrderid());
        Long appointmentId = db.queryForObject(
                "SELECT id FROM doctor_appointment WHERE appointment_no=? AND source='手机端' AND status='待确认'",
                Long.class, appointmentNo);
        assertNotNull(appointmentId);
        assertEquals(1L, db.queryForObject(
                "SELECT COUNT(*) FROM doctor_schedule WHERE appointment_id=?", Long.class, appointmentId));
        assertEquals(1L, db.queryForObject(
                "SELECT COUNT(*) FROM doctor_notification WHERE related_type='appointment' AND related_id=?",
                Long.class, appointmentId));

        assertEquals(1, ordersService.removeOrder(order.getOrderid()));
        assertEquals(3, db.queryForObject(
                "SELECT state FROM orders WHERE orderId=?", Integer.class, order.getOrderid()));
        assertEquals("已取消", db.queryForObject(
                "SELECT status FROM doctor_appointment WHERE id=?", String.class, appointmentId));
    }
}
