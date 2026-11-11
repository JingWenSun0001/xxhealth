package com.it.xxhealth.doctor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.*;
import java.time.*;
import java.util.*;

@Service
public class DoctorWorkspaceService {
    private final JdbcTemplate db;
    private final ObjectMapper json;
    private final DoctorOperationsService operations;
    private final DoctorMobileProjectionService mobileProjection;
    public DoctorWorkspaceService(JdbcTemplate db,ObjectMapper json,DoctorOperationsService operations,DoctorMobileProjectionService mobileProjection) { this.db=db; this.json=json; this.operations=operations; this.mobileProjection=mobileProjection; }
    private List<Map<String,Object>> rows(String sql,Object... args) {
        return db.query(sql, (rs,n)-> { Map<String,Object> row=new LinkedHashMap<>(); ResultSetMetaData meta=rs.getMetaData();
            for(int i=1;i<=meta.getColumnCount();i++) { Object value=rs.getObject(i); if(value instanceof java.sql.Date || value instanceof Timestamp) value=value.toString();
                row.put(JdbcUtils.convertUnderscoreNameToPropertyName(meta.getColumnLabel(i)),value); } return row; },args);
    }
    private Map<String,Object> one(String sql,Object... args) { List<Map<String,Object>> list=rows(sql,args); if(list.isEmpty()) throw new IllegalArgumentException("记录不存在"); return list.get(0); }
    private long count(String sql,Object... args) { Long n=db.queryForObject(sql,Long.class,args); return n==null?0:n; }
    private long insert(String sql,Object... args) { GeneratedKeyHolder key=new GeneratedKeyHolder(); db.update(con->{ PreparedStatement ps=con.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS); for(int i=0;i<args.length;i++) ps.setObject(i+1,args[i]); return ps; },key); return Objects.requireNonNull(key.getKey()).longValue(); }
    private String str(Map<String,?> m,String key,String fallback) { Object v=m.get(key); return v==null?fallback:v.toString().trim(); }
    private String required(Map<String,?> m,String key) { String v=str(m,key,""); if(v.isEmpty()) throw new IllegalArgumentException(key+"不能为空"); return v; }
    private long id(Map<String,?> m,String key) { try { return Long.parseLong(required(m,key)); } catch(NumberFormatException e) { throw new IllegalArgumentException("编号格式错误"); } }
    private void equal(StringBuilder where,List<Object> args,Map<String,String> q,String key,String column) { String v=q.get(key); if(v!=null&&!v.isEmpty()&&!v.equals("all")) { where.append(" AND ").append(column).append("=?"); args.add(v); } }
    private void like(StringBuilder where,List<Object> args,Map<String,String> q,String key,String column) { String v=q.get(key); if(v!=null&&!v.trim().isEmpty()) { where.append(" AND ").append(column).append(" LIKE ?"); args.add("%"+v.trim()+"%"); } }
    private Map<String,Object> page(String select,String from,StringBuilder where,List<Object> args,Map<String,String> q,String order) {
        int page,size; try { page=Math.max(1,Integer.parseInt(q.getOrDefault("page","1"))); size=Math.max(1,Math.min(500,Integer.parseInt(q.getOrDefault("pageSize","10")))); } catch(NumberFormatException e) { throw new IllegalArgumentException("分页参数错误"); }
        long total=count("SELECT COUNT(*) "+from+where,args.toArray()); List<Object> paged=new ArrayList<>(args); paged.add(size); paged.add((page-1)*size);
        Map<String,Object> result=new LinkedHashMap<>(); result.put("records",rows(select+from+where+order+" LIMIT ? OFFSET ?",paged.toArray())); result.put("total",total); result.put("page",page); result.put("pageSize",size); return result;
    }
    public Map<String,Object> patients(Map<String,String> q) {
        StringBuilder w=new StringBuilder(" WHERE deleted=0"); List<Object> a=new ArrayList<>();
        like(w,a,q,"name","name"); like(w,a,q,"phone","phone"); like(w,a,q,"identityCard","identity_card"); equal(w,a,q,"sex","sex"); equal(w,a,q,"healthStatus","health_status");
        if("today".equals(q.get("tab"))) { w.append(" AND last_exam_date=?"); a.add(LocalDate.now().toString()); }
        if("return".equals(q.get("tab"))) w.append(" AND visit_count>1");
        if("risk".equals(q.get("tab"))) w.append(" AND health_status='异常'");
        String age=q.get("age"); if("young".equals(age)) w.append(" AND age<35"); if("middle".equals(age)) w.append(" AND age BETWEEN 35 AND 59"); if("senior".equals(age)) w.append(" AND age>=60");
        if(q.containsKey("startDate")&&!q.get("startDate").isEmpty()) { w.append(" AND last_exam_date>=?"); a.add(q.get("startDate")); }
        if(q.containsKey("endDate")&&!q.get("endDate").isEmpty()) { w.append(" AND last_exam_date<=?"); a.add(q.get("endDate")); }
        Map<String,Object> result=page("SELECT * ","FROM doctor_patient",w,a,q," ORDER BY id ASC");
        result.put("stats",one("SELECT COUNT(*) total,COALESCE(SUM(DATE(created_at)=CURRENT_DATE),0) today,COALESCE(SUM(last_exam_date=CURRENT_DATE),0) today_visit,COALESCE(SUM(visit_count>1),0) returning,COALESCE(SUM(health_status='异常'),0) risk,COALESCE(SUM(follow_up=1),0) follow_up FROM doctor_patient WHERE deleted=0")); return result;
    }
    public Map<String,Object> patient(Long id) { Map<String,Object> p=one("SELECT * FROM doctor_patient WHERE id=? AND deleted=0",id); p.put("reports",rows("SELECT id,report_no,exam_date,package_name,status FROM doctor_report WHERE patient_id=? ORDER BY exam_date DESC",id)); return p; }
    @Transactional public Map<String,Object> savePatient(Long patientId,Map<String,Object> b) {
        String name=required(b,"name"),phone=required(b,"phone"),identity=required(b,"identityCard"),sex=required(b,"sex"),health=str(b,"healthStatus","正常");
        if(!phone.matches("1[3-9]\\d{9}")) throw new IllegalArgumentException("请输入有效的11位手机号");
        if(!identity.matches("(?:\\d{15}|\\d{17}[0-9Xx])")) throw new IllegalArgumentException("请输入有效的身份证号");
        if(!Arrays.asList("男","女").contains(sex)||!Arrays.asList("正常","异常","关注").contains(health)) throw new IllegalArgumentException("患者状态或性别错误");
        int age; try { age=Integer.parseInt(required(b,"age")); } catch(NumberFormatException e) { throw new IllegalArgumentException("年龄格式错误"); } if(age<0||age>150) throw new IllegalArgumentException("年龄超出范围");
        if(count("SELECT COUNT(*) FROM doctor_patient WHERE identity_card=? AND deleted=0 AND id<>?",identity,patientId==null?0:patientId)>0) throw new IllegalArgumentException("该身份证已建档");
        String date=str(b,"lastExamDate",""); if(!date.isEmpty()) LocalDate.parse(date);
        if(patientId==null) patientId=insert("INSERT INTO doctor_patient(name,sex,age,phone,identity_card,health_status,tags,last_exam_date,address,notes,avatar) VALUES(?,?,?,?,?,?,?,?,?,?,?)",name,sex,age,phone,identity,health,str(b,"tags","无"),date.isEmpty()?null:date,str(b,"address",""),str(b,"notes",""),sex.equals("男")?"male":"female");
        else { patient(patientId); db.update("UPDATE doctor_patient SET name=?,sex=?,age=?,phone=?,identity_card=?,health_status=?,tags=?,last_exam_date=?,address=?,notes=? WHERE id=? AND deleted=0",name,sex,age,phone,identity,health,str(b,"tags","无"),date.isEmpty()?null:date,str(b,"address",""),str(b,"notes",""),patientId); }
        return patient(patientId);
    }
    @Transactional public int importPatients(List<Map<String,Object>> rows) { if(rows.isEmpty()||rows.size()>500) throw new IllegalArgumentException("每次导入1至500条患者记录"); for(Map<String,Object> row:rows) savePatient(null,row); return rows.size(); }
    public void deletePatient(Long id) { patient(id); if(count("SELECT COUNT(*) FROM doctor_appointment WHERE patient_id=? AND status IN ('待确认','已确认')",id)>0) throw new IllegalArgumentException("患者有未完成预约，请先处理预约"); db.update("UPDATE doctor_patient SET deleted=1 WHERE id=?",id); }
    private final String appointmentFrom="FROM doctor_appointment a JOIN doctor_patient p ON p.id=a.patient_id";
    public Map<String,Object> appointment(Long id) { return one("SELECT a.*,p.name,p.sex,p.age,p.phone,p.avatar "+appointmentFrom+" WHERE a.id=?",id); }
    public Map<String,Object> appointments(Map<String,String> q) {
        StringBuilder w=new StringBuilder(" WHERE p.deleted=0"); List<Object> a=new ArrayList<>(); like(w,a,q,"name","p.name"); like(w,a,q,"phone","p.phone"); equal(w,a,q,"status","a.status"); equal(w,a,q,"packageName","a.package_name"); equal(w,a,q,"date","a.exam_date");
        Map<String,Object> result=page("SELECT a.*,p.name,p.sex,p.age,p.phone,p.avatar ",appointmentFrom,w,a,q," ORDER BY a.id ASC");
        result.put("stats",one("SELECT COUNT(*) total,COALESCE(SUM(a.status='待确认'),0) pending,COALESCE(SUM(a.status='已确认'),0) confirmed,COALESCE(SUM(a.status='已完成'),0) completed,COALESCE(SUM(a.status='已取消'),0) cancelled,COALESCE(SUM(a.exam_date=CURRENT_DATE),0) today,COALESCE(SUM(YEARWEEK(a.exam_date,1)=YEARWEEK(CURRENT_DATE,1)),0) week,COALESCE(SUM(DATE_FORMAT(a.exam_date,'%Y-%m')=DATE_FORMAT(CURRENT_DATE,'%Y-%m')),0) month "+appointmentFrom+" WHERE p.deleted=0")); return result;
    }
    public List<Map<String,Object>> calendar(String month) { YearMonth.parse(month); return rows("SELECT exam_date date,COUNT(*) count FROM doctor_appointment WHERE DATE_FORMAT(exam_date,'%Y-%m')=? AND status<>'已取消' GROUP BY exam_date",month); }
    @Transactional public Map<String,Object> createAppointment(Map<String,Object> b) {
        long pid=id(b,"patientId"); patient(pid); String date=required(b,"examDate"); LocalDate.parse(date); String slot=required(b,"timeSlot");
        if(!slot.matches("\\d{2}:\\d{2}–\\d{2}:\\d{2}")) throw new IllegalArgumentException("请选择有效时间段");
        String pack=required(b,"packageName");
        operations.validateBooking(pid,date,slot,pack);
        if(count("SELECT COUNT(*) FROM doctor_appointment WHERE patient_id=? AND exam_date=? AND status<>'已取消'",pid,date)>0) throw new IllegalArgumentException("该患者当天已有预约");
        long aid=insert("INSERT INTO doctor_appointment(patient_id,appointment_no,exam_date,time_slot,package_name,source,status) VALUES(?,CONCAT('H',REPLACE(?, '-', ''),RIGHT(UUID_SHORT(),6)),?,?,?,?, '待确认')",pid,date,date,slot,pack,str(b,"source","医生工作台")); operations.attachSchedule(aid); operations.appointmentNotification(aid,"预约提醒"); return one("SELECT * FROM doctor_appointment WHERE id=?",aid);
    }
    @Transactional public Map<String,Object> appointmentStatus(Long aid,Map<String,Object> b) {
        Map<String,Object> ap=one("SELECT * FROM doctor_appointment WHERE id=? FOR UPDATE",aid); String from=ap.get("status").toString(),to=required(b,"status");
        boolean valid=(from.equals("待确认")&&(to.equals("已确认")||to.equals("已取消")))||(from.equals("已确认")&&(to.equals("已完成")||to.equals("已取消")));
        if(!valid) throw new IllegalArgumentException("当前预约状态不支持此操作");
        db.update("UPDATE doctor_appointment SET status=? WHERE id=?",to,aid);
        operations.syncAppointmentStatus(aid,to); mobileProjection.syncAppointment(aid); operations.appointmentNotification(aid,"预约状态更新");
        if(to.equals("已完成")) db.update("UPDATE doctor_patient SET last_exam_date=?,visit_count=visit_count+1 WHERE id=?",ap.get("examDate"),ap.get("patientId"));
        return one("SELECT * FROM doctor_appointment WHERE id=?",aid);
    }
    private final String reportFrom="FROM doctor_report r JOIN doctor_patient p ON p.id=r.patient_id";
    public Map<String,Object> reports(Map<String,String> q) { StringBuilder w=new StringBuilder(" WHERE 1=1"); List<Object> a=new ArrayList<>(); like(w,a,q,"name","p.name"); equal(w,a,q,"patientId","r.patient_id"); equal(w,a,q,"status","r.status"); equal(w,a,q,"packageName","r.package_name");
        if("year".equals(q.get("range"))) w.append(" AND r.exam_date>=DATE_SUB(CURRENT_DATE,INTERVAL 1 YEAR)");
        return page("SELECT r.id,r.patient_id,r.report_no,r.exam_date,r.package_name,r.status,r.score,p.name,p.sex,p.age,p.avatar ",reportFrom,w,a,q," ORDER BY r.exam_date DESC,r.id DESC"); }
    private Object parse(String value) { try { return json.readValue(value,new TypeReference<List<Map<String,Object>>>(){}); } catch(Exception e) { throw new IllegalArgumentException("检查项目数据格式错误"); } }
    private String encode(Object value) { try { return json.writeValueAsString(value); } catch(Exception e) { throw new IllegalArgumentException("检查项目数据格式错误"); } }
    public Map<String,Object> report(Long rid) { Map<String,Object> r=one("SELECT r.*,p.name,p.sex,p.age,p.phone,p.avatar,p.identity_card "+reportFrom+" WHERE r.id=?",rid); r.put("items",parse(r.get("items").toString())); r.put("history",rows("SELECT id,exam_date,package_name,status FROM doctor_report WHERE patient_id=? ORDER BY exam_date DESC",r.get("patientId"))); return r; }
    @Transactional public Map<String,Object> createReport(Map<String,Object> b) {
        long pid=id(b,"patientId"); patient(pid); Map<String,Object> t=one("SELECT * FROM doctor_report_template WHERE id=?",id(b,"templateId"));
        String date=str(b,"examDate",LocalDate.now().toString()); LocalDate.parse(date);
        long rid=insert("INSERT INTO doctor_report(patient_id,report_no,exam_date,package_name,status,score,conclusion,advice,items) VALUES(?,CONCAT('HT',REPLACE(?,'-',''),RIGHT(UUID_SHORT(),6)),?,?,'草稿',95,?,?,?)",pid,date,date,t.get("name").toString().replace("报告","套餐"),str(b,"conclusion",""),t.get("advice"),t.get("items")); db.update("UPDATE doctor_report SET doctor_name=? WHERE id=?",operations.doctorName(),rid); return report(rid);
    }
    @Transactional public Map<String,Object> saveReport(Long rid,Map<String,Object> b) {
        Map<String,Object> r=one("SELECT * FROM doctor_report WHERE id=? FOR UPDATE",rid); if(!Arrays.asList("草稿","待审核").contains(r.get("status"))) throw new IllegalArgumentException("已生成报告不能修改，请新建报告");
        Object raw=b.get("items"); if(!(raw instanceof List)||((List<?>)raw).isEmpty()||((List<?>)raw).size()>200) throw new IllegalArgumentException("请填写检查项目");
        for(Object item:(List<?>)raw) { if(!(item instanceof Map)) throw new IllegalArgumentException("检查项格式错误"); Map<?,?> m=(Map<?,?>)item; if(m.get("name")==null||m.get("value")==null||m.get("group")==null) throw new IllegalArgumentException("检查项名称、分组和结果不能为空"); }
        int score; try { score=Integer.parseInt(required(b,"score")); } catch(NumberFormatException e) { throw new IllegalArgumentException("健康评分格式错误"); } if(score<0||score>100) throw new IllegalArgumentException("健康评分应在0到100之间");
        db.update("UPDATE doctor_report SET score=?,conclusion=?,advice=?,items=? WHERE id=?",score,str(b,"conclusion",""),str(b,"advice",""),encode(raw),rid); return report(rid);
    }
    @Transactional public Map<String,Object> reportStatus(Long rid,Map<String,Object> b) {
        Map<String,Object> r=one("SELECT * FROM doctor_report WHERE id=? FOR UPDATE",rid); String from=r.get("status").toString(),to=required(b,"status");
        if(!(from.equals("草稿")&&to.equals("待审核")||from.equals("待审核")&&(to.equals("已生成")||to.equals("草稿")))) throw new IllegalArgumentException("报告状态流转不合法");
        if(to.equals("待审核")||to.equals("已生成")) { if(str(r,"conclusion","").isEmpty()) throw new IllegalArgumentException("请填写体检结论后提交"); List<?> items=(List<?>)parse(r.get("items").toString()); for(Object item:items) if(str((Map<String,Object>)item,"value","").isEmpty()) throw new IllegalArgumentException("请填写全部检查结果后提交"); }
        db.update("UPDATE doctor_report SET status=?,reported_at=CURRENT_TIMESTAMP WHERE id=?",to,rid);
        if(to.equals("已生成")) {
            operations.notify("报告通知","报告通知","体检报告 "+r.get("reportNo")+" 已生成，请查看完整检查结果。","report",rid);
            List<?> items=(List<?>)parse(r.get("items").toString()); boolean abnormal=false;
            for(Object raw:items) {
                Map<String,Object> item=(Map<String,Object>)raw;
                if(Boolean.TRUE.equals(item.get("abnormal"))) {
                    abnormal=true; String name=str(item,"name",""); String type=name.contains("血糖")?"血糖异常":name.contains("胆固醇")||name.contains("脂蛋白")||name.contains("甘油三酯")?"血脂异常":"体检异常";
                    long alertId=insert("INSERT INTO doctor_alert(patient_id,report_id,type,indicator) VALUES(?,?,?,?)",r.get("patientId"),rid,type,name+" "+str(item,"value","")+" "+str(item,"unit",""));
                    operations.notify("异常提醒","异常提醒",name+"指标异常，请查看报告并处理。","alert",alertId);
                }
            }
            db.update("UPDATE doctor_patient SET last_exam_date=CASE WHEN last_exam_date IS NULL OR last_exam_date<? THEN ? ELSE last_exam_date END,health_status=?,follow_up=? WHERE id=?",r.get("examDate"),r.get("examDate"),abnormal?"异常":"正常",abnormal?1:0,r.get("patientId"));
            mobileProjection.syncPublishedReport(rid);
        }
        return report(rid);
    }
    public List<Map<String,Object>> templates() { List<Map<String,Object>> list=rows("SELECT * FROM doctor_report_template ORDER BY id"); for(Map<String,Object> t:list) t.put("items",parse(t.get("items").toString())); return list; }
    public Map<String,Object> createTemplate(Map<String,Object> b) { String name=required(b,"name"); Object items=b.get("items"); if(!(items instanceof List)||((List<?>)items).isEmpty()) throw new IllegalArgumentException("模板检查项不能为空"); long tid=insert("INSERT INTO doctor_report_template(name,category,description,advice,items) VALUES(?,?,?,?,?)",name,str(b,"category","常规模板"),str(b,"description","自定义体检模板"),str(b,"advice",""),encode(items)); return one("SELECT id,name FROM doctor_report_template WHERE id=?",tid); }
    public Map<String,Object> alerts(Map<String,String> q) { StringBuilder w=new StringBuilder(" WHERE 1=1"); List<Object> a=new ArrayList<>(); equal(w,a,q,"type","a.type"); like(w,a,q,"name","p.name"); return page("SELECT a.*,p.name,p.age,p.sex ","FROM doctor_alert a JOIN doctor_patient p ON p.id=a.patient_id",w,a,q," ORDER BY a.created_at DESC"); }
    @Transactional public void resolveAlert(Long aid) {
        Map<String,Object> alert=one("SELECT id,patient_id FROM doctor_alert WHERE id=?",aid);
        db.update("UPDATE doctor_alert SET resolved=1 WHERE id=?",aid);
        if(count("SELECT COUNT(*) FROM doctor_alert WHERE patient_id=? AND resolved=0",alert.get("patientId"))==0)
            db.update("UPDATE doctor_patient SET follow_up=0 WHERE id=?",alert.get("patientId"));
    }
    public Map<String,Object> dashboard() { Map<String,Object> d=new LinkedHashMap<>();
        d.put("stats",one("SELECT (SELECT COUNT(*) FROM doctor_report WHERE status='待审核') pending_reports,(SELECT COUNT(*) FROM doctor_appointment WHERE exam_date=CURRENT_DATE AND status<>'已取消') today_appointments,(SELECT COUNT(*) FROM doctor_patient WHERE deleted=0) patients,(SELECT COUNT(*) FROM doctor_alert WHERE resolved=0) unread_alerts"));
        d.put("tasks",rows("SELECT r.id,r.status,r.package_name,r.exam_date,p.name,p.sex,p.age FROM doctor_report r JOIN doctor_patient p ON p.id=r.patient_id WHERE r.status IN ('草稿','待审核') ORDER BY r.id LIMIT 3"));
        d.put("alerts",rows("SELECT a.*,p.name FROM doctor_alert a JOIN doctor_patient p ON p.id=a.patient_id WHERE resolved=0 ORDER BY created_at DESC LIMIT 3"));
        d.put("trend",rows("SELECT exam_date date,COUNT(*) reports,SUM(JSON_CONTAINS(items,'{\"abnormal\":true}')) abnormal FROM doctor_report WHERE exam_date BETWEEN DATE_SUB(CURRENT_DATE,INTERVAL 6 DAY) AND CURRENT_DATE GROUP BY exam_date ORDER BY exam_date"));
        d.put("doctor",operations.doctorName()); d.put("date",LocalDate.now().toString()); return d;
    }
}
