package com.it.xxhealth.doctor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/** Catalogue, appointments' scheduling, inbox and persistent workspace preferences. */
@Service
public class DoctorOperationsService {
    private final JdbcTemplate db;
    private final ObjectMapper json;
    private final DoctorMobileProjectionService mobileProjection;
    public DoctorOperationsService(JdbcTemplate db, ObjectMapper json, DoctorMobileProjectionService mobileProjection) { this.db = db; this.json = json; this.mobileProjection = mobileProjection; }

    private List<Map<String,Object>> rows(String sql, Object... args) {
        return db.query(sql, (rs, n) -> {
            Map<String,Object> row = new LinkedHashMap<>();
            for (int i=1;i<=rs.getMetaData().getColumnCount();i++) {
                Object value = rs.getObject(i);
                if (value instanceof java.sql.Date || value instanceof Time || value instanceof Timestamp) value=value.toString();
                row.put(JdbcUtils.convertUnderscoreNameToPropertyName(rs.getMetaData().getColumnLabel(i)),value);
            }
            return row;
        }, args);
    }
    private Map<String,Object> one(String sql, Object... args) {
        List<Map<String,Object>> result=rows(sql,args);
        if(result.isEmpty()) throw new IllegalArgumentException("记录不存在，请刷新后重试");
        return result.get(0);
    }
    private long count(String sql, Object... args) { Long n=db.queryForObject(sql,Long.class,args); return n==null?0:n; }
    private long insert(String sql, Object... args) {
        GeneratedKeyHolder key=new GeneratedKeyHolder();
        db.update(con -> { PreparedStatement ps=con.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS); for(int i=0;i<args.length;i++) ps.setObject(i+1,args[i]); return ps; },key);
        return Objects.requireNonNull(key.getKey()).longValue();
    }
    private String text(Map<String,?> b,String key,String fallback) { Object v=b.get(key);return v==null?fallback:v.toString().trim(); }
    private String required(Map<String,?> b,String key) { String s=text(b,key,"");if(s.isEmpty()) throw new IllegalArgumentException("请填写"+key);return s; }
    private int number(Object value,String label) { try {return Integer.parseInt(String.valueOf(value));} catch(Exception e){throw new IllegalArgumentException(label+"格式不正确");} }
    private long id(Object value) { try{return Long.parseLong(String.valueOf(value));}catch(Exception e){throw new IllegalArgumentException("编号格式不正确");} }
    private Object decode(Object value) { try{return json.readValue(value.toString(),Object.class);}catch(Exception e){throw new IllegalArgumentException("数据格式不正确");} }
    private String encode(Object value) { try{return json.writeValueAsString(value);}catch(Exception e){throw new IllegalArgumentException("数据格式不正确");} }
    @SuppressWarnings("unchecked") private Map<String,Object> object(Object v) { if(!(v instanceof Map))throw new IllegalArgumentException("配置格式错误");return (Map<String,Object>)v; }
    private List<String> strings(Object value,int limit) {
        if(!(value instanceof List)||((List<?>)value).isEmpty()||((List<?>)value).size()>limit)throw new IllegalArgumentException("请填写有效的套餐标签和检查项目");
        List<String> result=new ArrayList<>();for(Object v:(List<?>)value){if(!(v instanceof String)||v.toString().trim().isEmpty()||v.toString().length()>100)throw new IllegalArgumentException("项目名称或标签格式错误");result.add(v.toString().trim());}return result;
    }
    private void equal(StringBuilder w,List<Object> a,Map<String,String> q,String key,String column) { String v=q.get(key);if(v!=null&&!v.isEmpty()&&!"all".equals(v)){w.append(" AND ").append(column).append("=?");a.add(v);} }
    private Map<String,Object> page(String select,String from,StringBuilder w,List<Object> a,Map<String,String> q,String order) {
        int p=Math.max(1,number(q.getOrDefault("page","1"),"页码")),size=Math.max(1,Math.min(500,number(q.getOrDefault("pageSize","10"),"每页条数")));
        long total=count("SELECT COUNT(*) "+from+w,a.toArray());List<Object> args=new ArrayList<>(a);args.add(size);args.add((long)(p-1)*size);
        Map<String,Object> result=new LinkedHashMap<>();result.put("records",rows(select+from+w+order+" LIMIT ? OFFSET ?",args.toArray()));result.put("total",total);result.put("page",p);result.put("pageSize",size);return result;
    }
    private void unpackPackage(Map<String,Object> p) { p.put("features",decode(p.get("features")));p.put("items",decode(p.get("items"))); }
    public Map<String,Object> packages(Map<String,String> q) {
        StringBuilder w=new StringBuilder(" WHERE 1=1");List<Object>a=new ArrayList<>();
        equal(w,a,q,"category","category");equal(w,a,q,"enabled","enabled");
        if(q.containsKey("gender")&&!q.get("gender").isEmpty()){w.append(" AND (gender='全部' OR gender=?)");a.add(q.get("gender"));}
        String keyword=q.getOrDefault("keyword","").trim();if(!keyword.isEmpty()){w.append(" AND (name LIKE ? OR audience LIKE ? OR CAST(items AS CHAR) LIKE ?)");for(int i=0;i<3;i++)a.add("%"+keyword+"%");}
        String price=q.get("price");if("low".equals(price))w.append(" AND price<500");if("middle".equals(price))w.append(" AND price>=500 AND price<1000");if("high".equals(price))w.append(" AND price>=1000");
        String feature=q.get("feature");if(feature!=null&&!feature.isEmpty()){w.append(" AND JSON_CONTAINS(features,JSON_QUOTE(?))");a.add(feature);}
        String order=" ORDER BY sort_order,id";if("priceAsc".equals(q.get("sort")))order=" ORDER BY price,id";if("priceDesc".equals(q.get("sort")))order=" ORDER BY price DESC,id";if("newest".equals(q.get("sort")))order=" ORDER BY id DESC";
        Map<String,Object> result=page("SELECT * ","FROM doctor_package",w,a,q,order);
        for(Object p:(List<?>)result.get("records"))unpackPackage(object(p));
        result.put("categories",rows("SELECT category,COUNT(*) count FROM doctor_package GROUP BY category ORDER BY MIN(sort_order)"));return result;
    }
    public Map<String,Object> packageDetail(long pid){Map<String,Object>p=one("SELECT * FROM doctor_package WHERE id=?",pid);unpackPackage(p);return p;}
    @Transactional public Map<String,Object> savePackage(Long pid,Map<String,Object>b){
        String name=required(b,"name"),category=required(b,"category"),gender=text(b,"gender","全部"),audience=required(b,"audience");
        if(!Arrays.asList("基础体检","中青年体检","高端体检","女性专项","男性专项","老年体检","职场体检").contains(category)||!Arrays.asList("全部","男","女").contains(gender))throw new IllegalArgumentException("套餐分类或适用人群错误");
        BigDecimal price,original;try{price=new BigDecimal(required(b,"price"));original=new BigDecimal(required(b,"originalPrice"));}catch(Exception e){throw new IllegalArgumentException("价格格式不正确");}
        if(price.signum()<0||original.compareTo(price)<0||original.compareTo(new BigDecimal("999999"))>0||price.scale()>2||original.scale()>2)throw new IllegalArgumentException("价格须为两位以内小数，原价不能低于售价");
        List<String>features=strings(b.get("features"),4),items=strings(b.get("items"),40);
        String picture=text(b,"picture","basic");if(!Arrays.asList("basic","middle","senior","women","men","premium").contains(picture))throw new IllegalArgumentException("套餐封面无效");
        int enabled=number(b.getOrDefault("enabled",1),"上架状态");if(enabled!=0&&enabled!=1)throw new IllegalArgumentException("上架状态错误");
        if(pid==null)pid=insert("INSERT INTO doctor_package(name,category,audience,gender,price,original_price,features,items,picture,enabled,sort_order) VALUES(?,?,?,?,?,?,?,?,?,?,?)",name,category,audience,gender,price,original,encode(features),encode(items),picture,enabled,number(b.getOrDefault("sortOrder",0),"排序"));
        else {Map<String,Object> old=one("SELECT * FROM doctor_package WHERE id=? FOR UPDATE",pid);checkVersion(old,b);db.update("UPDATE doctor_package SET name=?,category=?,audience=?,gender=?,price=?,original_price=?,features=?,items=?,picture=?,enabled=?,sort_order=?,version=version+1 WHERE id=?",name,category,audience,gender,price,original,encode(features),encode(items),picture,enabled,number(b.getOrDefault("sortOrder",0),"排序"),pid);}
        audit("体检套餐","保存套餐："+name);return packageDetail(pid);
    }
    @Transactional public Map<String,Object> packageStatus(long pid,Map<String,Object>b){Map<String,Object>p=one("SELECT * FROM doctor_package WHERE id=? FOR UPDATE",pid);checkVersion(p,b);int enabled=number(b.get("enabled"),"上架状态");if(enabled!=0&&enabled!=1)throw new IllegalArgumentException("上架状态错误");db.update("UPDATE doctor_package SET enabled=?,version=version+1 WHERE id=?",enabled,pid);audit("体检套餐",(enabled==1?"上架":"下架")+p.get("name"));return packageDetail(pid);}
    private void checkVersion(Map<String,Object> old,Map<String,Object>b){if(number(b.get("version"),"版本号")!=((Number)old.get("version")).intValue())throw new IllegalArgumentException("数据已被更新，请刷新页面后重试");}

    public Map<String,Object> settings(){Map<String,Object>s=one("SELECT * FROM doctor_settings WHERE id=1");for(String k:Arrays.asList("profile","preferences","notifications","rules"))s.put(k,decode(s.get(k)));return s;}
    public String doctorName(){return text(object(settings().get("profile")),"doctorName","张医生");}
    public Map<String,Object> rules(){return object(settings().get("rules"));}
    @Transactional public Map<String,Object> saveSettings(String section,Map<String,Object>b){
        if(!Arrays.asList("profile","preferences","notifications","rules").contains(section))throw new IllegalArgumentException("未知设置分类");
        Map<String,Object> old=one("SELECT * FROM doctor_settings WHERE id=1 FOR UPDATE");checkVersion(old,b);
        Map<String,Object> values=new LinkedHashMap<>(object(decode(old.get(section))));Map<String,Object> incoming=object(b.get("values"));
        for(String key:incoming.keySet()){if(!values.containsKey(key))throw new IllegalArgumentException("未知设置项："+key);values.put(key,incoming.get(key));}
        if(section.equals("profile")){
            for(String key:Arrays.asList("doctorName","department","title","hospital")){if(required(values,key).length()>60)throw new IllegalArgumentException("名称过长");}
            if(!text(values,"phone","").matches("(?:|1[3-9]\\d{9})"))throw new IllegalArgumentException("请输入有效手机号");
            if(!text(values,"email","").matches("(?:|[^\\s@]+@[^\\s@]+\\.[^\\s@]+)"))throw new IllegalArgumentException("邮箱格式不正确");
            if(text(values,"bio","").length()>1000)throw new IllegalArgumentException("个人简介最多1000字");
        } else if(section.equals("preferences")){
            if(!Arrays.asList("dashboard","appointments","reports","patients","packages","schedule","notifications","settings").contains(required(values,"landingPage"))||!Arrays.asList(10,20,50).contains(number(values.get("pageSize"),"每页条数"))||!Arrays.asList("day","week","month").contains(required(values,"calendarView")))throw new IllegalArgumentException("工作偏好选项无效");
            if(required(values,"defaultRoom").length()>60)throw new IllegalArgumentException("诊室名称过长");
        } else if(section.equals("notifications")){for(Object value:values.values())if(!(value instanceof Boolean))throw new IllegalArgumentException("通知开关格式错误");
        } else {
            int capacity=number(values.get("dailyCapacity"),"每日容量"),slot=number(values.get("slotMinutes"),"时段长度");
            if(capacity<1||capacity>300||!Arrays.asList(15,30,60).contains(slot))throw new IllegalArgumentException("每日容量范围1–300，时段长度为15、30或60分钟");
            LocalTime ms=LocalTime.parse(required(values,"morningStart")),me=LocalTime.parse(required(values,"morningEnd")),as=LocalTime.parse(required(values,"afternoonStart")),ae=LocalTime.parse(required(values,"afternoonEnd"));
            if(!ms.isBefore(me)||me.isAfter(as)||!as.isBefore(ae))throw new IllegalArgumentException("工作时间范围有误或存在交叉");
            Object days=values.get("workdays");if(!(days instanceof List)||((List<?>)days).isEmpty())throw new IllegalArgumentException("至少选择一个工作日");
            Set<Integer> unique=new HashSet<>();for(Object d:(List<?>)days){int day=number(d,"工作日");if(day<1||day>7||!unique.add(day))throw new IllegalArgumentException("工作日设置无效");}
            if(text(values,"bookingNotes","").length()>1000)throw new IllegalArgumentException("预约须知最多1000字");
        }
        db.update("UPDATE doctor_settings SET "+section+"=?,version=version+1 WHERE id=1",encode(values));audit("系统设置","更新"+section+"设置");return settings();
    }
    private void audit(String module,String action){db.update("INSERT INTO doctor_operation_log(actor,module,action) VALUES(?,?,?)",doctorName(),module,action);}
    public Map<String,Object> auditLogs(Map<String,String>q){return page("SELECT * ","FROM doctor_operation_log",new StringBuilder(" WHERE 1=1"),new ArrayList<>(),q," ORDER BY id DESC");}

    public Map<String,Object> notificationSummary(){Map<String,Object>r=one("SELECT COUNT(*) total,COALESCE(SUM(is_read=0),0) unread,COALESCE(SUM(is_read=1),0) `read` FROM doctor_notification");r.put("categories",rows("SELECT category,COUNT(*) count,SUM(is_read=0) unread FROM doctor_notification GROUP BY category"));return r;}
    public Map<String,Object> notifications(Map<String,String>q){StringBuilder w=new StringBuilder(" WHERE 1=1");List<Object>a=new ArrayList<>();equal(w,a,q,"category","category");equal(w,a,q,"isRead","is_read");String keyword=q.getOrDefault("keyword","").trim();if(!keyword.isEmpty()){w.append(" AND (title LIKE ? OR content LIKE ?)");a.add("%"+keyword+"%");a.add("%"+keyword+"%");}if("3months".equals(q.get("range")))w.append(" AND created_at>=DATE_SUB(CURRENT_TIMESTAMP,INTERVAL 3 MONTH)");if("month".equals(q.get("range")))w.append(" AND created_at>=DATE_SUB(CURRENT_TIMESTAMP,INTERVAL 1 MONTH)");Map<String,Object>r=page("SELECT * ","FROM doctor_notification",w,a,q," ORDER BY created_at DESC,id DESC");r.put("summary",notificationSummary());return r;}
    public Map<String,Object> notification(long nid){return one("SELECT * FROM doctor_notification WHERE id=?",nid);}
    public void readNotification(long nid,Map<String,Object>b){notification(nid);if(!(b.get("read") instanceof Boolean))throw new IllegalArgumentException("已读状态格式错误");db.update("UPDATE doctor_notification SET is_read=? WHERE id=?",Boolean.TRUE.equals(b.get("read"))?1:0,nid);}
    public int readAll(){return db.update("UPDATE doctor_notification SET is_read=1 WHERE is_read=0");}
    public void notify(String category,String title,String content,String relatedType,Long relatedId){
        String key=new HashMap<String,String>(){{put("预约提醒","appointment");put("报告通知","report");put("异常提醒","alert");put("系统通知","system");put("其他消息","other");}}.get(category);
        if(key==null)throw new IllegalArgumentException("消息分类无效");
        if(Boolean.FALSE.equals(object(settings().get("notifications")).get(key)))return;
        db.update("INSERT INTO doctor_notification(category,title,content,related_type,related_id) VALUES(?,?,?,?,?)",category,title,content,relatedType,relatedId);
    }
    public void appointmentNotification(long aid,String title){Map<String,Object>a=one("SELECT a.appointment_no,a.exam_date,a.time_slot,p.name FROM doctor_appointment a JOIN doctor_patient p ON p.id=a.patient_id WHERE a.id=?",aid);String name=a.get("name").toString();if(name.length()>1)name=name.charAt(0)+"*"+name.substring(name.length()-1);notify("预约提醒",title,name+"，体检时间："+a.get("examDate")+" "+a.get("timeSlot")+"，请查看日程安排。","appointment",aid);}

    private final String scheduleJoin=" FROM doctor_schedule s JOIN doctor_appointment a ON a.id=s.appointment_id JOIN doctor_patient p ON p.id=a.patient_id ";
    private String scheduleSelect(){return "SELECT s.*,a.patient_id,a.appointment_no,a.package_name,p.name,p.sex,p.age,p.phone,p.avatar"+scheduleJoin;}
    public Map<String,Object> schedule(Map<String,String>q){
        LocalDate date=LocalDate.parse(q.getOrDefault("date",LocalDate.now().toString()));String view=q.getOrDefault("view","day");LocalDate from=date,to=date;
        if(view.equals("week")){from=date.minusDays(date.getDayOfWeek().getValue()-1);to=from.plusDays(6);}else if(view.equals("month")){from=date.withDayOfMonth(1);to=from.plusMonths(1).minusDays(1);}else if(!view.equals("day"))throw new IllegalArgumentException("日历视图无效");
        Map<String,Object>r=new LinkedHashMap<>();r.put("events",rows(scheduleSelect()+" WHERE s.schedule_date BETWEEN ? AND ? AND s.status<>'已取消' AND p.deleted=0 ORDER BY s.schedule_date,s.start_time,s.id",from.toString(),to.toString()));
        r.put("calendar",rows("SELECT schedule_date date,COUNT(*) count,SUM(status='已完成') completed FROM doctor_schedule WHERE schedule_date BETWEEN ? AND ? AND status<>'已取消' GROUP BY schedule_date",date.withDayOfMonth(1).toString(),date.withDayOfMonth(1).plusMonths(1).minusDays(1).toString()));
        r.put("stats",one("SELECT COUNT(*) total,COALESCE(SUM(status IN ('已预约','待检')),0) pending,COALESCE(SUM(status='已完成'),0) completed,COALESCE(SUM(status='进行中'),0) in_progress FROM doctor_schedule WHERE schedule_date=? AND status<>'已取消'",date.toString()));
        r.put("previous",one("SELECT COUNT(*) total,COALESCE(SUM(status IN ('已预约','待检')),0) pending,COALESCE(SUM(status='已完成'),0) completed FROM doctor_schedule WHERE schedule_date=? AND status<>'已取消'",date.minusDays(1).toString()));
        r.put("alerts",rows("SELECT a.*,p.name,p.sex,p.age FROM doctor_alert a JOIN doctor_patient p ON p.id=a.patient_id WHERE a.resolved=0 ORDER BY a.created_at DESC LIMIT 30"));r.put("rules",rules());r.put("date",date.toString());return r;
    }
    private void lockDate(String date){db.update("INSERT IGNORE INTO doctor_schedule_day(schedule_date) VALUES(?)",date);one("SELECT schedule_date FROM doctor_schedule_day WHERE schedule_date=? FOR UPDATE",date);}
    public void validateBooking(long patientId,String date,String slot,String packageName){
        LocalDate day=LocalDate.parse(date);String[] times=slot.split("–");if(times.length!=2)throw new IllegalArgumentException("预约时段格式不正确");
        validateHours(day,LocalTime.parse(times[0]),LocalTime.parse(times[1]));lockDate(date);
        if(count("SELECT COUNT(*) FROM doctor_package WHERE name=? AND enabled=1",packageName)==0)throw new IllegalArgumentException("该套餐不存在或已下架，请重新选择");
        String gender=one("SELECT gender FROM doctor_package WHERE name=?",packageName).get("gender").toString();
        if(!gender.equals("全部")&&!gender.equals(one("SELECT sex FROM doctor_patient WHERE id=?",patientId).get("sex")))throw new IllegalArgumentException("患者性别不符合所选套餐的适用人群");
        if(count("SELECT COUNT(*) FROM doctor_appointment WHERE exam_date=? AND status<>'已取消'",date)>=number(rules().get("dailyCapacity"),"每日容量"))throw new IllegalArgumentException("当日预约已满，请选择其他日期");
        String room=text(object(settings().get("preferences")),"defaultRoom","体检一室");
        if(count("SELECT COUNT(*) FROM doctor_schedule WHERE schedule_date=? AND room=? AND start_time<? AND end_time>? AND status<>'已取消'",date,room,times[1],times[0])>0)throw new IllegalArgumentException("该诊室在所选时段已有安排，请选择其他时段");
    }
    private void validateHours(LocalDate date,LocalTime start,LocalTime end){
        Map<String,Object> r=rules();boolean workday=false;for(Object day:(List<?>)r.get("workdays"))if(number(day,"工作日")==date.getDayOfWeek().getValue())workday=true;
        if(!workday)throw new IllegalArgumentException("所选日期为非工作日");
        LocalTime ms=LocalTime.parse(r.get("morningStart").toString()),me=LocalTime.parse(r.get("morningEnd").toString()),as=LocalTime.parse(r.get("afternoonStart").toString()),ae=LocalTime.parse(r.get("afternoonEnd").toString());
        boolean morning=!start.isBefore(ms)&&!end.isAfter(me),afternoon=!start.isBefore(as)&&!end.isAfter(ae);
        if(!start.isBefore(end)||!(morning||afternoon))throw new IllegalArgumentException("预约时间应在工作时段内，且结束时间晚于开始时间");
        int minutes=number(r.get("slotMinutes"),"时段长度");if(Duration.between(start,end).toMinutes()%minutes!=0)throw new IllegalArgumentException("预约时长须为"+minutes+"分钟的整数倍");
    }
    public void attachSchedule(long aid){Map<String,Object>a=one("SELECT * FROM doctor_appointment WHERE id=?",aid);String[]time=a.get("timeSlot").toString().split("–");String room=text(object(settings().get("preferences")),"defaultRoom","体检一室");db.update("INSERT IGNORE INTO doctor_schedule(appointment_id,schedule_date,start_time,end_time,room) VALUES(?,?,?,?,?)",aid,a.get("examDate"),time[0],time[1],room);}
    public void syncAppointmentStatus(long aid,String status){db.update("UPDATE doctor_schedule SET status=?,version=version+1 WHERE appointment_id=?",status.equals("已完成")?"已完成":status.equals("已取消")?"已取消":"已预约",aid);}
    private Map<String,Object> scheduleEvent(long sid){return one(scheduleSelect()+" WHERE s.id=?",sid);}
    @Transactional public Map<String,Object> updateSchedule(long sid,Map<String,Object>b){
        Map<String,Object> current=one("SELECT * FROM doctor_schedule WHERE id=? FOR UPDATE",sid);checkVersion(current,b);
        if(Arrays.asList("已完成","已取消").contains(current.get("status")))throw new IllegalArgumentException("已完成或已取消日程不能调整");
        String date=text(b,"scheduleDate",current.get("scheduleDate").toString()),room=text(b,"room",current.get("room").toString()),state=text(b,"status",current.get("status").toString());
        LocalTime start=LocalTime.parse(text(b,"startTime",current.get("startTime").toString())),end=LocalTime.parse(text(b,"endTime",current.get("endTime").toString()));
        if(!Arrays.asList("已预约","待检","进行中","已完成","已取消").contains(state))throw new IllegalArgumentException("日程状态无效");
        if(room.isEmpty()||room.length()>60||text(b,"notes","").length()>1000)throw new IllegalArgumentException("诊室或备注格式错误");
        if(!state.equals("已取消")){validateHours(LocalDate.parse(date),start,end);lockDate(date);checkSlot(sid,id(current.get("appointmentId")),date,start,end,room,Collections.singletonList(sid));}
        if("进行中".equals(current.get("status"))&&(!current.get("scheduleDate").equals(date)||!LocalTime.parse(current.get("startTime").toString()).equals(start)||!LocalTime.parse(current.get("endTime").toString()).equals(end)))throw new IllegalArgumentException("进行中的体检不能更改日期或时段");
        db.update("UPDATE doctor_schedule SET schedule_date=?,start_time=?,end_time=?,room=?,status=?,notes=?,version=version+1 WHERE id=?",date,start.toString(),end.toString(),room,state,text(b,"notes",current.get("notes").toString()),sid);
        long aid=id(current.get("appointmentId"));String appointmentState=state.equals("已完成")?"已完成":state.equals("已取消")?"已取消":"已确认";
        db.update("UPDATE doctor_appointment SET exam_date=?,time_slot=?,status=? WHERE id=?",date,start.toString()+"–"+end.toString(),appointmentState,aid);
        mobileProjection.syncAppointment(aid);
        if(state.equals("已完成")){Map<String,Object>a=one("SELECT patient_id FROM doctor_appointment WHERE id=?",aid);db.update("UPDATE doctor_patient SET last_exam_date=?,visit_count=visit_count+1 WHERE id=?",date,a.get("patientId"));}
        appointmentNotification(aid,"日程更新通知");audit("日程管理","更新日程 #"+sid+"："+state);return scheduleEvent(sid);
    }
    private void checkSlot(long sid,long aid,String date,LocalTime start,LocalTime end,String room,List<Long>excluded){
        String placeholders=String.join(",",Collections.nCopies(excluded.size(),"?"));List<Object>args=new ArrayList<>(Arrays.asList(date,room,end.toString(),start.toString()));args.addAll(excluded);
        if(count("SELECT COUNT(*) FROM doctor_schedule WHERE schedule_date=? AND room=? AND start_time<? AND end_time>? AND status<>'已取消' AND id NOT IN ("+placeholders+")",args.toArray())>0)throw new IllegalArgumentException("目标诊室在该时段已有安排，请更换时段或诊室");
        Map<String,Object>a=one("SELECT patient_id,exam_date FROM doctor_appointment WHERE id=?",aid);
        if(count("SELECT COUNT(*) FROM doctor_appointment WHERE patient_id=? AND exam_date=? AND id<>? AND status<>'已取消'",a.get("patientId"),date,aid)>0)throw new IllegalArgumentException("患者在目标日期已有预约");
        if(!date.equals(a.get("examDate"))&&count("SELECT COUNT(*) FROM doctor_appointment WHERE exam_date=? AND status<>'已取消'",date)>=number(rules().get("dailyCapacity"),"每日容量"))throw new IllegalArgumentException("目标日期预约已满");
    }
    @Transactional public int batchSchedule(Map<String,Object>b){
        Object raw=b.get("events");if(!(raw instanceof List)||((List<?>)raw).isEmpty()||((List<?>)raw).size()>100)throw new IllegalArgumentException("请选择1至100条日程");
        int offset=number(b.getOrDefault("offsetMinutes",0),"调整分钟数");if(Math.abs(offset)>720)throw new IllegalArgumentException("调整范围不能超过12小时");String targetDate=text(b,"targetDate","");
        List<Long>ids=new ArrayList<>();List<Map<String,Object>>events=new ArrayList<>();for(Object value:(List<?>)raw){Map<String,Object>v=object(value);long sid=id(v.get("id"));if(ids.contains(sid))throw new IllegalArgumentException("不能重复选择日程");ids.add(sid);Map<String,Object>e=one("SELECT * FROM doctor_schedule WHERE id=? FOR UPDATE",sid);checkVersion(e,v);if(!Arrays.asList("已预约","待检").contains(e.get("status")))throw new IllegalArgumentException("仅已预约、待检日程支持批量调整");events.add(e);}
        Set<String>dates=new TreeSet<>();for(Map<String,Object>e:events)dates.add(targetDate.isEmpty()?e.get("scheduleDate").toString():targetDate);for(String date:dates){LocalDate.parse(date);lockDate(date);}
        List<Map<String,Object>>updated=new ArrayList<>();for(Map<String,Object>e:events){String date=targetDate.isEmpty()?e.get("scheduleDate").toString():targetDate;LocalTime start=LocalTime.parse(e.get("startTime").toString()).plusMinutes(offset),end=LocalTime.parse(e.get("endTime").toString()).plusMinutes(offset);validateHours(LocalDate.parse(date),start,end);checkSlot(id(e.get("id")),id(e.get("appointmentId")),date,start,end,e.get("room").toString(),ids);Map<String,Object>v=new LinkedHashMap<>(e);v.put("scheduleDate",date);v.put("startTime",start.toString());v.put("endTime",end.toString());updated.add(v);}
        Set<String>patientDates=new HashSet<>();
        for(int i=0;i<updated.size();i++){
            Map<String,Object>event=updated.get(i);Object patient=one("SELECT patient_id FROM doctor_appointment WHERE id=?",event.get("appointmentId")).get("patientId");
            if(!patientDates.add(event.get("scheduleDate")+":"+patient))throw new IllegalArgumentException("批量调整后同一患者在同一天存在重复预约");
            for(int j=0;j<i;j++){
                Map<String,Object>other=updated.get(j);
                if(event.get("scheduleDate").equals(other.get("scheduleDate"))&&event.get("room").equals(other.get("room"))&&LocalTime.parse(event.get("startTime").toString()).isBefore(LocalTime.parse(other.get("endTime").toString()))&&LocalTime.parse(event.get("endTime").toString()).isAfter(LocalTime.parse(other.get("startTime").toString())))throw new IllegalArgumentException("所选日程调整后在同一诊室发生时段冲突");
            }
        }
        for(String date:dates){
            List<Object>args=new ArrayList<>();args.add(date);for(Map<String,Object>e:events)args.add(e.get("appointmentId"));
            long existing=count("SELECT COUNT(*) FROM doctor_appointment WHERE exam_date=? AND status<>'已取消' AND id NOT IN ("+String.join(",",Collections.nCopies(events.size(),"?"))+")",args.toArray());
            long moved=updated.stream().filter(e->date.equals(e.get("scheduleDate"))).count();
            if(existing+moved>number(rules().get("dailyCapacity"),"每日容量"))throw new IllegalArgumentException("批量调整后将超过目标日期预约容量");
        }
        for(Map<String,Object>e:updated){db.update("UPDATE doctor_schedule SET schedule_date=?,start_time=?,end_time=?,version=version+1 WHERE id=?",e.get("scheduleDate"),e.get("startTime"),e.get("endTime"),e.get("id"));db.update("UPDATE doctor_appointment SET exam_date=?,time_slot=? WHERE id=?",e.get("scheduleDate"),e.get("startTime")+"–"+e.get("endTime"),e.get("appointmentId"));mobileProjection.syncAppointment(id(e.get("appointmentId")));appointmentNotification(id(e.get("appointmentId")),"日程批量调整通知");}
        audit("日程管理","批量调整"+updated.size()+"条日程");return updated.size();
    }
}
