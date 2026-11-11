package com.it.xxhealth.doctor;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DoctorWorkspaceIntegrationTests {
    @Autowired DoctorWorkspaceService service;
    @Autowired DoctorOperationsService operations;
    @Autowired JdbcTemplate db;
    @Autowired MockMvc mvc;
    private Long patientId;
    private Map<String,Object> patientData() {
        Map<String,Object> b=new LinkedHashMap<>(); b.put("name","联调测试患者"); b.put("sex","女"); b.put("age",32); b.put("phone","13888889999"); b.put("identityCard","110101199401019999"); b.put("healthStatus","正常"); return b;
    }
    private Map<String,Object> map(Object... pairs) { Map<String,Object> m=new LinkedHashMap<>(); for(int i=0;i<pairs.length;i+=2)m.put(pairs[i].toString(),pairs[i+1]);return m; }
    private String workingDate(int index) {
        Map<String,Object> settings=operations.settings();
        Set<Integer> workdays=new HashSet<>();
        for(Object day:(List<?>)((Map<?,?>)settings.get("rules")).get("workdays")) workdays.add(Integer.parseInt(day.toString()));
        LocalDate date=LocalDate.of(2027,1,1);
        while(true){if(workdays.contains(date.getDayOfWeek().getValue())&&index--==0)return date.toString();date=date.plusDays(1);}
    }
    @BeforeEach void setup() { patientId=((Number)service.savePatient(null,patientData()).get("id")).longValue(); }
    @Test void patientCrudAndFiltering() {
        Map<String,Object> data=patientData();data.put("name","更新测试患者");data.put("healthStatus","异常");service.savePatient(patientId,data);
        Map<String,String> q=new HashMap<>();q.put("name","更新测试");q.put("healthStatus","异常");q.put("sex","女");assertEquals(1L,service.patients(q).get("total"));
        service.deletePatient(patientId);assertThrows(IllegalArgumentException.class,()->service.patient(patientId));
    }
    @Test void duplicateIdentityIsRejected() { assertThrows(IllegalArgumentException.class,()->service.savePatient(null,patientData())); }
    @Test void phoneValidationIsEnforced() { Map<String,Object> b=patientData();b.put("phone","123");assertThrows(IllegalArgumentException.class,()->service.savePatient(null,b)); }
    @Test void appointmentLifecycleAndPatientVisitUpdate() {
        String date=workingDate(0);Map<String,Object> a=service.createAppointment(map("patientId",patientId,"examDate",date,"timeSlot","09:00–10:00","packageName","基础体检套餐"));long aid=((Number)a.get("id")).longValue();
        assertThrows(IllegalArgumentException.class,()->service.deletePatient(patientId));
        assertEquals("已确认",service.appointmentStatus(aid,map("status","已确认")).get("status"));
        service.appointmentStatus(aid,map("status","已完成"));assertEquals(date,service.patient(patientId).get("lastExamDate"));
        assertThrows(IllegalArgumentException.class,()->service.appointmentStatus(aid,map("status","已取消")));
    }
    @Test void duplicateDailyAppointmentIsRejected() {
        Map<String,Object> a=map("patientId",patientId,"examDate",workingDate(1),"timeSlot","09:00–10:00","packageName","基础体检套餐");service.createAppointment(a);assertThrows(IllegalArgumentException.class,()->service.createAppointment(a));
    }
    @Test void reportRequiresCompleteResultsThenBecomesImmutable() {
        Map<String,Object> item=map("name","血糖","group","生化检查","value","","unit","mmol/L","range","3.9–6.1","abnormal",false);
        Map<String,Object> t=service.createTemplate(map("name","联调测试模板","items",Collections.singletonList(item)));
        Map<String,Object> r=service.createReport(map("patientId",patientId,"templateId",t.get("id")));long rid=((Number)r.get("id")).longValue();
        assertThrows(IllegalArgumentException.class,()->service.reportStatus(rid,map("status","待审核")));
        item.put("value","5.1");service.saveReport(rid,map("score",95,"conclusion","检查结果基本正常","advice","定期体检","items",Collections.singletonList(item)));
        service.reportStatus(rid,map("status","待审核"));assertEquals("已生成",service.reportStatus(rid,map("status","已生成")).get("status"));
        assertThrows(IllegalArgumentException.class,()->service.saveReport(rid,map("score",90)));
        assertEquals(1,((List<?>)service.patient(patientId).get("reports")).size());
    }
    @Test void paginationAndSqlInjectionInputAreHandled() {
        Map<String,String> q=new HashMap<>();q.put("name","' OR 1=1 --");q.put("pageSize","5000");Map<String,Object> p=service.patients(q);assertEquals(0L,p.get("total"));assertEquals(500,p.get("pageSize"));
    }
    @Test void approvalCreatesAlertsAndUpdatesPatientHealth() {
        Map<String,Object> item=map("name","总胆固醇","group","生化检查","value","5.8","unit","mmol/L","range","≤5.2","abnormal",true);
        Map<String,Object> t=service.createTemplate(map("name","异常指标测试模板","items",Collections.singletonList(item)));
        Map<String,Object> r=service.createReport(map("patientId",patientId,"templateId",t.get("id"),"conclusion","请复查血脂"));long rid=((Number)r.get("id")).longValue();
        service.reportStatus(rid,map("status","待审核"));service.reportStatus(rid,map("status","已生成"));
        assertEquals("异常",service.patient(patientId).get("healthStatus"));
        assertEquals(1L,db.queryForObject("SELECT COUNT(*) FROM doctor_alert WHERE report_id=? AND type='血脂异常'",Long.class,rid));
    }
    @Test void apiUsesCamelCaseAndReturnsValidationErrors() throws Exception {
        mvc.perform(get("/doctor/workspace/dashboard")).andExpect(status().isOk()).andExpect(jsonPath("$.data.stats.pendingReports").isNumber());
        mvc.perform(get("/doctor/workspace/calendar").param("month","bad-date")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(400));
        mvc.perform(post("/doctor/workspace/patients").contentType("application/json").content("{}" )).andExpect(status().isBadRequest());
    }
}
