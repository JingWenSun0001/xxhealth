package com.it.xxhealth.doctor;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DoctorOperationsIntegrationTests {
    @Autowired DoctorOperationsService operations;
    @Autowired DoctorWorkspaceService workspace;
    @Autowired JdbcTemplate db;
    @Autowired MockMvc mvc;
    private long patientId;
    private Map<String,Object> map(Object...pairs){Map<String,Object>m=new LinkedHashMap<>();for(int i=0;i<pairs.length;i+=2)m.put(pairs[i].toString(),pairs[i+1]);return m;}
    private long longValue(Object v){return ((Number)v).longValue();}
    @SuppressWarnings("unchecked") private Map<String,Object> object(Object v){return (Map<String,Object>)v;}
    @BeforeEach void fixture(){
        patientId=longValue(workspace.savePatient(null,map("name","运营模块测试","sex","女","age",30,"phone","13899990000","identityCard","110101199601019888")).get("id"));
        Map<String,Object>s=operations.settings();Map<String,Object>rules=object(s.get("rules"));rules.put("workdays",Arrays.asList(1,2,3,4,5,6,7));rules.put("dailyCapacity",40);rules.put("slotMinutes",30);rules.put("morningStart","08:00");rules.put("morningEnd","12:00");rules.put("afternoonStart","13:30");rules.put("afternoonEnd","17:00");operations.saveSettings("rules",map("version",s.get("version"),"values",rules));
        s=operations.settings();operations.saveSettings("notifications",map("version",s.get("version"),"values",map("appointment",true,"report",true,"alert",true,"system",true,"other",true)));
    }
    private Map<String,Object> appointment(String date,String slot){return workspace.createAppointment(map("patientId",patientId,"examDate",date,"timeSlot",slot,"packageName","基础体检套餐"));}
    private Map<String,Object> eventFor(long aid){List<?>events=(List<?>)operations.schedule(Collections.singletonMap("date","2027-02-02")).get("events");return events.stream().map(this::object).filter(e->longValue(e.get("appointmentId"))==aid).findFirst().orElseThrow(AssertionError::new);}
    private Map<String,Object> samplePackage(){return map("name","测试专属体检套餐","category","基础体检","audience","测试适用人群","gender","全部","price","499.00","originalPrice","699.00","features",Arrays.asList("热门","推荐"),"items",Arrays.asList("一般检查","血常规"),"picture","basic","enabled",1,"sortOrder",0);}
    @Test void catalogueCrudFilterAndOptimisticVersion(){Map<String,Object>p=operations.savePackage(null,samplePackage());long pid=longValue(p.get("id"));Map<String,String>q=new HashMap<>();q.put("keyword","测试专属");q.put("feature","热门");q.put("price","low");assertEquals(1L,operations.packages(q).get("total"));p.put("price","459.00");Map<String,Object>saved=operations.savePackage(pid,p);assertEquals(1,((Number)saved.get("version")).intValue());assertThrows(IllegalArgumentException.class,()->operations.savePackage(pid,p));}
    @Test void catalogueRejectsInvalidPriceAndInvalidJsonItems(){Map<String,Object>p=samplePackage();p.put("price",-1);assertThrows(IllegalArgumentException.class,()->operations.savePackage(null,p));p.put("price",499);p.put("items",Arrays.asList(5));assertThrows(IllegalArgumentException.class,()->operations.savePackage(null,p));}
    @Test void disabledPackageRejectsNewBookingsAndPreservesHistory(){Map<String,Object>p=operations.savePackage(null,samplePackage());Map<String,Object>a=workspace.createAppointment(map("patientId",patientId,"examDate","2027-02-03","timeSlot","09:00–10:00","packageName",p.get("name")));operations.packageStatus(longValue(p.get("id")),map("version",p.get("version"),"enabled",0));assertNotNull(workspace.appointments(Collections.singletonMap("date","2027-02-03")));assertThrows(IllegalArgumentException.class,()->workspace.createAppointment(map("patientId",patientId,"examDate","2027-02-04","timeSlot","09:00–10:00","packageName",p.get("name"))));assertEquals(p.get("name"),db.queryForObject("SELECT package_name FROM doctor_appointment WHERE id=?",String.class,a.get("id")));}
    @Test void bookingCreatesLinkedScheduleAndNotification(){long before=db.queryForObject("SELECT COUNT(*) FROM doctor_notification",Long.class);Map<String,Object>a=appointment("2027-02-02","09:00–10:00");assertNotNull(eventFor(longValue(a.get("id"))));assertEquals(before+1,db.queryForObject("SELECT COUNT(*) FROM doctor_notification",Long.class));}
    @Test void timeValidationAndDailyCapacityAreEnforced(){assertThrows(IllegalArgumentException.class,()->appointment("2027-02-02","12:00–13:00"));Map<String,Object>s=operations.settings();operations.saveSettings("rules",map("version",s.get("version"),"values",map("dailyCapacity",1)));appointment("2027-02-02","09:00–10:00");assertThrows(IllegalArgumentException.class,()->appointment("2027-02-02","10:00–11:00"));}
    @Test void scheduleAdjustmentUpdatesAppointmentAndRejectsStaleVersion(){Map<String,Object>a=appointment("2027-02-02","09:00–10:00"),e=eventFor(longValue(a.get("id")));long sid=longValue(e.get("id"));Map<String,Object>updated=operations.updateSchedule(sid,map("version",e.get("version"),"startTime","10:00","endTime","11:00","status","待检"));assertEquals("10:00:00",updated.get("startTime"));assertEquals("10:00–11:00",db.queryForObject("SELECT time_slot FROM doctor_appointment WHERE id=?",String.class,a.get("id")));assertThrows(IllegalArgumentException.class,()->operations.updateSchedule(sid,map("version",e.get("version"))));}
    @Test void roomConflictsAreRejected(){Map<String,Object>a=appointment("2027-02-02","09:00–10:00"),e=eventFor(longValue(a.get("id")));Map<String,Object>b=workspace.savePatient(null,map("name","第二位测试患者","sex","男","age",35,"phone","13999990000","identityCard","110101199101019777"));workspace.createAppointment(map("patientId",b.get("id"),"examDate","2027-02-02","timeSlot","10:00–11:00","packageName","基础体检套餐"));assertThrows(IllegalArgumentException.class,()->operations.updateSchedule(longValue(e.get("id")),map("version",e.get("version"),"startTime","10:00","endTime","11:00")));}
    @Test void completionLocksScheduleAndUpdatesPatientOnce(){Map<String,Object>a=appointment("2027-02-02","09:00–10:00"),e=eventFor(longValue(a.get("id")));Map<String,Object>done=operations.updateSchedule(longValue(e.get("id")),map("version",e.get("version"),"status","已完成"));assertEquals("2027-02-02",workspace.patient(patientId).get("lastExamDate"));assertThrows(IllegalArgumentException.class,()->operations.updateSchedule(longValue(e.get("id")),map("version",done.get("version"),"status","待检")));}
    @Test void batchAdjustsAtomicallyAndChecksCapacity(){Map<String,Object>a=appointment("2027-02-02","09:00–10:00"),e=eventFor(longValue(a.get("id")));Map<String,Object>batch=map("events",Collections.singletonList(map("id",e.get("id"),"version",e.get("version"))),"targetDate","2027-02-03","offsetMinutes",60);assertEquals(1,operations.batchSchedule(batch));assertEquals("2027-02-03",db.queryForObject("SELECT CAST(exam_date AS CHAR) FROM doctor_appointment WHERE id=?",String.class,a.get("id")));}
    @Test void batchFailureDoesNotChangeAnySchedule(){Map<String,Object>a=appointment("2027-02-02","09:00–10:00"),e=eventFor(longValue(a.get("id")));assertThrows(IllegalArgumentException.class,()->operations.batchSchedule(map("events",Collections.singletonList(map("id",e.get("id"),"version",e.get("version"))),"targetDate","2027-02-03","offsetMinutes",600)));assertEquals("2027-02-02",db.queryForObject("SELECT CAST(schedule_date AS CHAR) FROM doctor_schedule WHERE id=?",String.class,e.get("id")));}
    @Test void notificationReadAllSearchAndPreferenceArePersistent(){operations.notify("其他消息","测试消息标题","独有搜索词","",null);Map<String,String>q=new HashMap<>();q.put("keyword","独有搜索词");Map<String,Object>page=operations.notifications(q);assertEquals(1L,page.get("total"));Map<String,Object>n=object(((List<?>)page.get("records")).get(0));operations.readNotification(longValue(n.get("id")),map("read",true));assertEquals(1,((Number)operations.notification(longValue(n.get("id"))).get("isRead")).intValue());operations.readAll();assertEquals(0L,longValue(operations.notificationSummary().get("unread")));Map<String,Object>s=operations.settings();operations.saveSettings("notifications",map("version",s.get("version"),"values",map("appointment",false)));long before=db.queryForObject("SELECT COUNT(*) FROM doctor_notification",Long.class);appointment("2027-02-02","09:00–10:00");assertEquals(before,db.queryForObject("SELECT COUNT(*) FROM doctor_notification",Long.class));}
    @Test void settingsValidationVersionAndAudit(){Map<String,Object>s=operations.settings();Map<String,Object>updated=operations.saveSettings("profile",map("version",s.get("version"),"values",map("doctorName","王医生")));assertEquals("王医生",operations.doctorName());assertThrows(IllegalArgumentException.class,()->operations.saveSettings("profile",map("version",s.get("version"),"values",map("doctorName","旧版本"))));assertThrows(IllegalArgumentException.class,()->operations.saveSettings("rules",map("version",updated.get("version"),"values",map("morningEnd","07:00"))));assertTrue(longValue(operations.auditLogs(Collections.emptyMap()).get("total"))>0);}
    @Test void apiContractsAndBadParameters() throws Exception {mvc.perform(get("/doctor/workspace/packages")).andExpect(status().isOk()).andExpect(jsonPath("$.data.records[0].features").isArray());mvc.perform(get("/doctor/workspace/notifications/summary")).andExpect(jsonPath("$.data.unread").isNumber());mvc.perform(get("/doctor/workspace/schedule").param("date","invalid")).andExpect(status().isBadRequest());mvc.perform(put("/doctor/workspace/settings/unknown").contentType("application/json").content("{}" )).andExpect(status().isBadRequest());}
    @Test void genderSpecificPackageChecksPatientEligibility(){assertThrows(IllegalArgumentException.class,()->workspace.createAppointment(map("patientId",patientId,"examDate","2027-02-02","timeSlot","09:00–10:00","packageName","男性健康套餐")));Map<String,String>q=new HashMap<>();q.put("gender","女");q.put("keyword","基础体检套餐");assertEquals(1L,operations.packages(q).get("total"));}
    @Test void mergingDaysCannotDuplicatePatientOrOverlapRoom(){
        Map<String,Object>a=appointment("2027-02-02","09:00–10:00"),e=eventFor(longValue(a.get("id"))),b=appointment("2027-02-03","09:00–10:00");
        Long sid=db.queryForObject("SELECT id FROM doctor_schedule WHERE appointment_id=?",Long.class,b.get("id"));
        assertThrows(IllegalArgumentException.class,()->operations.batchSchedule(map("events",Arrays.asList(map("id",e.get("id"),"version",e.get("version")),map("id",sid,"version",0)),"targetDate","2027-02-04","offsetMinutes",0)));
        assertEquals(0L,db.queryForObject("SELECT COUNT(*) FROM doctor_appointment WHERE id IN (?,?) AND exam_date='2027-02-04'",Long.class,a.get("id"),b.get("id")));
    }
    @Test void batchCannotOverflowCombinedDailyCapacity(){
        Map<String,Object>a=appointment("2027-02-02","09:00–10:00"),e=eventFor(longValue(a.get("id")));
        Map<String,Object>p=workspace.savePatient(null,map("name","批量第二患者","sex","男","age",33,"phone","13799990000","identityCard","110101199301019666"));
        Map<String,Object>b=workspace.createAppointment(map("patientId",p.get("id"),"examDate","2027-02-02","timeSlot","10:00–11:00","packageName","基础体检套餐")),e2=eventFor(longValue(b.get("id")));
        Map<String,Object>s=operations.settings();operations.saveSettings("rules",map("version",s.get("version"),"values",map("dailyCapacity",1)));
        assertThrows(IllegalArgumentException.class,()->operations.batchSchedule(map("events",Arrays.asList(map("id",e.get("id"),"version",e.get("version")),map("id",e2.get("id"),"version",e2.get("version"))),"targetDate","2027-02-03","offsetMinutes",0)));
        assertEquals(2L,db.queryForObject("SELECT COUNT(*) FROM doctor_appointment WHERE id IN (?,?) AND exam_date='2027-02-02'",Long.class,a.get("id"),b.get("id")));
    }
}
