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

/**
 * 医生端运营业务服务，负责套餐、日程、通知、设置及操作审计。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
@Service
public class DoctorOperationsService {
    /**
     * 执行参数化 SQL 和事务内数据库操作的 JdbcTemplate。
     */
    private final JdbcTemplate db;
    /**
     * 负责 Java 对象与 JSON 字符串互转的序列化组件。
     */
    private final ObjectMapper json;
    /**
     * 将医生端状态变化投影回用户端数据的服务。
     */
    private final DoctorMobileProjectionService mobileProjection;
    /**
     * 创建 DoctorOperationsService 实例并注入所需依赖。
     * @param db 数据库访问组件
     * @param json JSON 序列化组件
     * @param mobileProjection 医生端到用户端的数据投影服务
     */
    public DoctorOperationsService(JdbcTemplate db, ObjectMapper json, DoctorMobileProjectionService mobileProjection) { this.db = db; this.json = json; this.mobileProjection = mobileProjection; }

    /**
     * 执行参数化查询，并将结果集转换为驼峰命名的键值集合。
     * @param sql 包含占位符的 SQL 语句
     * @param args 应用启动参数或 SQL 绑定参数
     * @return 符合条件的数据集合
     */
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
    /**
     * 执行参数化查询并返回唯一一条记录。
     * @param sql 包含占位符的 SQL 语句
     * @param args 应用启动参数或 SQL 绑定参数
     * @return 包含查询结果或业务状态的键值数据
     * @throws IllegalArgumentException 当参数格式错误、记录不存在或不满足业务规则时抛出
     */
    private Map<String,Object> one(String sql, Object... args) {
        List<Map<String,Object>> result=rows(sql,args);
        if(result.isEmpty()) throw new IllegalArgumentException("记录不存在，请刷新后重试");
        return result.get(0);
    }
    /**
     * 执行统计 SQL 并返回记录数量。
     * @param sql 包含占位符的 SQL 语句
     * @param args 应用启动参数或 SQL 绑定参数
     * @return 处理结果数值
     */
    private long count(String sql, Object... args) { Long n=db.queryForObject(sql,Long.class,args); return n==null?0:n; }
    /**
     * 执行插入 SQL 并返回数据库生成的主键。
     * @param sql 包含占位符的 SQL 语句
     * @param args 应用启动参数或 SQL 绑定参数
     * @return 处理结果数值
     */
    private long insert(String sql, Object... args) {
        GeneratedKeyHolder key=new GeneratedKeyHolder();
        db.update(con -> { PreparedStatement ps=con.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS); for(int i=0;i<args.length;i++) ps.setObject(i+1,args[i]); return ps; },key);
        return Objects.requireNonNull(key.getKey()).longValue();
    }
    /**
     * 读取键值并转换为去除首尾空白的文本。
     * @param b 请求体键值数据
     * @param key 请求字段名
     * @param fallback 字段缺失时使用的默认值
     * @return 方法处理结果
     */
    private String text(Map<String,?> b,String key,String fallback) { Object v=b.get(key);return v==null?fallback:v.toString().trim(); }
    /**
     * 读取必填文本字段，并在缺失时拒绝请求。
     * @param b 请求体键值数据
     * @param key 请求字段名
     * @return 方法处理结果
     * @throws IllegalArgumentException 当参数格式错误、记录不存在或不满足业务规则时抛出
     */
    private String required(Map<String,?> b,String key) { String s=text(b,key,"");if(s.isEmpty()) throw new IllegalArgumentException("请填写"+key);return s; }
    /**
     * 将输入值转换为整数，并提供统一的格式错误提示。
     * @param value 待转换或校验的输入值
     * @param label 用于错误提示的参数名称
     * @return 处理结果数值
     * @throws IllegalArgumentException 当参数格式错误、记录不存在或不满足业务规则时抛出
     */
    private int number(Object value,String label) { try {return Integer.parseInt(String.valueOf(value));} catch(Exception e){throw new IllegalArgumentException(label+"格式不正确");} }
    /**
     * 将输入值转换为长整型业务编号。
     * @param value 待转换或校验的输入值
     * @return 处理结果数值
     * @throws IllegalArgumentException 当参数格式错误、记录不存在或不满足业务规则时抛出
     */
    private long id(Object value) { try{return Long.parseLong(String.valueOf(value));}catch(Exception e){throw new IllegalArgumentException("编号格式不正确");} }
    /**
     * 将数据库中的 JSON 内容反序列化为 Java 对象。
     * @param value 待转换或校验的输入值
     * @return 方法处理结果
     * @throws IllegalArgumentException 当参数格式错误、记录不存在或不满足业务规则时抛出
     */
    private Object decode(Object value) { try{return json.readValue(value.toString(),Object.class);}catch(Exception e){throw new IllegalArgumentException("数据格式不正确");} }
    /**
     * 将 Java 对象序列化为可写入数据库的 JSON 文本。
     * @param value 待转换或校验的输入值
     * @return 方法处理结果
     * @throws IllegalArgumentException 当参数格式错误、记录不存在或不满足业务规则时抛出
     */
    private String encode(Object value) { try{return json.writeValueAsString(value);}catch(Exception e){throw new IllegalArgumentException("数据格式不正确");} }
    /**
     * 校验并转换键值对象。
     * @param v 待校验或转换的对象
     * @return 包含查询结果或业务状态的键值数据
     * @throws IllegalArgumentException 当参数格式错误、记录不存在或不满足业务规则时抛出
     */
    @SuppressWarnings("unchecked") private Map<String,Object> object(Object v) { if(!(v instanceof Map))throw new IllegalArgumentException("配置格式错误");return (Map<String,Object>)v; }
    /**
     * 校验字符串集合的类型、数量和内容长度。
     * @param value 待转换或校验的输入值
     * @param limit 集合允许的最大元素数量
     * @return 符合条件的数据集合
     * @throws IllegalArgumentException 当参数格式错误、记录不存在或不满足业务规则时抛出
     */
    private List<String> strings(Object value,int limit) {
        if(!(value instanceof List)||((List<?>)value).isEmpty()||((List<?>)value).size()>limit)throw new IllegalArgumentException("请填写有效的套餐标签和检查项目");
        List<String> result=new ArrayList<>();for(Object v:(List<?>)value){if(!(v instanceof String)||v.toString().trim().isEmpty()||v.toString().length()>100)throw new IllegalArgumentException("项目名称或标签格式错误");result.add(v.toString().trim());}return result;
    }
    /**
     * 向动态查询追加等值筛选条件和绑定参数。
     * @param w 动态 WHERE 条件
     * @param a 动态 SQL 的绑定参数集合
     * @param q 查询字符串参数
     * @param key 请求字段名
     * @param column 服务端指定的数据库列名
     */
    private void equal(StringBuilder w,List<Object> a,Map<String,String> q,String key,String column) { String v=q.get(key);if(v!=null&&!v.isEmpty()&&!"all".equals(v)){w.append(" AND ").append(column).append("=?");a.add(v);} }
    /**
     * 执行带总数统计的数据库分页查询。
     * @param select 分页查询的 SELECT 片段
     * @param from 分页查询的 FROM 片段
     * @param w 动态 WHERE 条件
     * @param a 动态 SQL 的绑定参数集合
     * @param q 查询字符串参数
     * @param order 经过白名单选择的排序片段
     * @return 包含查询结果或业务状态的键值数据
     */
    private Map<String,Object> page(String select,String from,StringBuilder w,List<Object> a,Map<String,String> q,String order) {
        int p=Math.max(1,number(q.getOrDefault("page","1"),"页码")),size=Math.max(1,Math.min(500,number(q.getOrDefault("pageSize","10"),"每页条数")));
        long total=count("SELECT COUNT(*) "+from+w,a.toArray());List<Object> args=new ArrayList<>(a);args.add(size);args.add((long)(p-1)*size);
        Map<String,Object> result=new LinkedHashMap<>();result.put("records",rows(select+from+w+order+" LIMIT ? OFFSET ?",args.toArray()));result.put("total",total);result.put("page",p);result.put("pageSize",size);return result;
    }
    /**
     * 将套餐中的 JSON 标签和项目解析为集合。
     * @param p 待处理的套餐或键值对象
     */
    private void unpackPackage(Map<String,Object> p) { p.put("features",decode(p.get("features")));p.put("items",decode(p.get("items"))); }
    /**
     * 查询当前可见的体检套餐，并按筛选条件返回分页或移动端列表。
     * @param q 查询字符串参数
     * @return 包含查询结果或业务状态的键值数据
     */
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
    /**
     * 查询指定的已上架体检套餐详情。
     * @param pid 患者或套餐主键
     * @return 包含查询结果或业务状态的键值数据
     */
    public Map<String,Object> packageDetail(long pid){Map<String,Object>p=one("SELECT * FROM doctor_package WHERE id=?",pid);unpackPackage(p);return p;}
    /**
     * 校验套餐价格、标签、检查项和版本号后新增或更新套餐。
     * @param pid 患者或套餐主键
     * @param b 请求体键值数据
     * @return 包含查询结果或业务状态的键值数据
     * @throws IllegalArgumentException 当参数格式错误、记录不存在或不满足业务规则时抛出
     */
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
    /**
     * 校验版本号并更新套餐上架状态。
     * @param pid 患者或套餐主键
     * @param b 请求体键值数据
     * @return 包含查询结果或业务状态的键值数据
     * @throws IllegalArgumentException 当参数格式错误、记录不存在或不满足业务规则时抛出
     */
    @Transactional public Map<String,Object> packageStatus(long pid,Map<String,Object>b){Map<String,Object>p=one("SELECT * FROM doctor_package WHERE id=? FOR UPDATE",pid);checkVersion(p,b);int enabled=number(b.get("enabled"),"上架状态");if(enabled!=0&&enabled!=1)throw new IllegalArgumentException("上架状态错误");db.update("UPDATE doctor_package SET enabled=?,version=version+1 WHERE id=?",enabled,pid);audit("体检套餐",(enabled==1?"上架":"下架")+p.get("name"));return packageDetail(pid);}
    /**
     * 校验客户端版本号，防止旧数据覆盖并发修改。
     * @param old 数据库中的当前版本数据
     * @param b 请求体键值数据
     * @throws IllegalArgumentException 当参数格式错误、记录不存在或不满足业务规则时抛出
     */
    private void checkVersion(Map<String,Object> old,Map<String,Object>b){if(number(b.get("version"),"版本号")!=((Number)old.get("version")).intValue())throw new IllegalArgumentException("数据已被更新，请刷新页面后重试");}

    /**
     * 读取医生工作台的个人资料、偏好、通知和排班规则。
     * @return 包含查询结果或业务状态的键值数据
     */
    public Map<String,Object> settings(){Map<String,Object>s=one("SELECT * FROM doctor_settings WHERE id=1");for(String k:Arrays.asList("profile","preferences","notifications","rules"))s.put(k,decode(s.get(k)));return s;}
    /**
     * 读取系统设置中当前医生的显示姓名。
     * @return 方法处理结果
     */
    public String doctorName(){return text(object(settings().get("profile")),"doctorName","张医生");}
    /**
     * 读取并解析当前排班规则。
     * @return 包含查询结果或业务状态的键值数据
     */
    public Map<String,Object> rules(){return object(settings().get("rules"));}
    /**
     * 按设置分区合并、校验并持久化工作台配置。
     * @param section 系统设置分区名称
     * @param b 请求体键值数据
     * @return 包含查询结果或业务状态的键值数据
     * @throws IllegalArgumentException 当参数格式错误、记录不存在或不满足业务规则时抛出
     */
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
    /**
     * 记录医生端关键操作的审计日志。
     * @param module module 参数
     * @param action action 参数
     */
    private void audit(String module,String action){db.update("INSERT INTO doctor_operation_log(actor,module,action) VALUES(?,?,?)",doctorName(),module,action);}
    /**
     * 分页查询医生端操作审计日志。
     * @param q 查询字符串参数
     * @return 包含查询结果或业务状态的键值数据
     */
    public Map<String,Object> auditLogs(Map<String,String>q){return page("SELECT * ","FROM doctor_operation_log",new StringBuilder(" WHERE 1=1"),new ArrayList<>(),q," ORDER BY id DESC");}

    /**
     * 统计消息总数、已读数、未读数及分类数量。
     * @return 包含查询结果或业务状态的键值数据
     */
    public Map<String,Object> notificationSummary(){Map<String,Object>r=one("SELECT COUNT(*) total,COALESCE(SUM(is_read=0),0) unread,COALESCE(SUM(is_read=1),0) `read` FROM doctor_notification");r.put("categories",rows("SELECT category,COUNT(*) count,SUM(is_read=0) unread FROM doctor_notification GROUP BY category"));return r;}
    /**
     * 按分类、状态、关键字和时间范围分页查询消息。
     * @param q 查询字符串参数
     * @return 包含查询结果或业务状态的键值数据
     */
    public Map<String,Object> notifications(Map<String,String>q){StringBuilder w=new StringBuilder(" WHERE 1=1");List<Object>a=new ArrayList<>();equal(w,a,q,"category","category");equal(w,a,q,"isRead","is_read");String keyword=q.getOrDefault("keyword","").trim();if(!keyword.isEmpty()){w.append(" AND (title LIKE ? OR content LIKE ?)");a.add("%"+keyword+"%");a.add("%"+keyword+"%");}if("3months".equals(q.get("range")))w.append(" AND created_at>=DATE_SUB(CURRENT_TIMESTAMP,INTERVAL 3 MONTH)");if("month".equals(q.get("range")))w.append(" AND created_at>=DATE_SUB(CURRENT_TIMESTAMP,INTERVAL 1 MONTH)");Map<String,Object>r=page("SELECT * ","FROM doctor_notification",w,a,q," ORDER BY created_at DESC,id DESC");r.put("summary",notificationSummary());return r;}
    /**
     * 查询指定消息的完整内容。
     * @param nid 消息主键
     * @return 包含查询结果或业务状态的键值数据
     */
    public Map<String,Object> notification(long nid){return one("SELECT * FROM doctor_notification WHERE id=?",nid);}
    /**
     * 更新指定消息的已读状态。
     * @param nid 消息主键
     * @param b 请求体键值数据
     * @throws IllegalArgumentException 当参数格式错误、记录不存在或不满足业务规则时抛出
     */
    public void readNotification(long nid,Map<String,Object>b){notification(nid);if(!(b.get("read") instanceof Boolean))throw new IllegalArgumentException("已读状态格式错误");db.update("UPDATE doctor_notification SET is_read=? WHERE id=?",Boolean.TRUE.equals(b.get("read"))?1:0,nid);}
    /**
     * 将所有未读消息批量标记为已读。
     * @return 处理结果数值
     */
    public int readAll(){return db.update("UPDATE doctor_notification SET is_read=1 WHERE is_read=0");}
    /**
     * 根据通知偏好创建一条医生端站内消息。
     * @param category 消息分类
     * @param title 消息标题
     * @param content 消息正文
     * @param relatedType 关联业务类型
     * @param relatedId 关联业务主键
     * @throws IllegalArgumentException 当参数格式错误、记录不存在或不满足业务规则时抛出
     */
    public void notify(String category,String title,String content,String relatedType,Long relatedId){
        String key=new HashMap<String,String>(){{put("预约提醒","appointment");put("报告通知","report");put("异常提醒","alert");put("系统通知","system");put("其他消息","other");}}.get(category);
        if(key==null)throw new IllegalArgumentException("消息分类无效");
        if(Boolean.FALSE.equals(object(settings().get("notifications")).get(key)))return;
        db.update("INSERT INTO doctor_notification(category,title,content,related_type,related_id) VALUES(?,?,?,?,?)",category,title,content,relatedType,relatedId);
    }
    /**
     * 根据预约与患者信息创建脱敏的预约通知。
     * @param aid 预约或异常提醒主键
     * @param title 消息标题
     */
    public void appointmentNotification(long aid,String title){Map<String,Object>a=one("SELECT a.appointment_no,a.exam_date,a.time_slot,p.name FROM doctor_appointment a JOIN doctor_patient p ON p.id=a.patient_id WHERE a.id=?",aid);String name=a.get("name").toString();if(name.length()>1)name=name.charAt(0)+"*"+name.substring(name.length()-1);notify("预约提醒",title,name+"，体检时间："+a.get("examDate")+" "+a.get("timeSlot")+"，请查看日程安排。","appointment",aid);}

    /**
     * 日程查询共用的关联表 SQL 片段。
     */
    private final String scheduleJoin=" FROM doctor_schedule s JOIN doctor_appointment a ON a.id=s.appointment_id JOIN doctor_patient p ON p.id=a.patient_id ";
    /**
     * 构造日程详情查询使用的公共 SELECT 片段。
     * @return 方法处理结果
     */
    private String scheduleSelect(){return "SELECT s.*,a.patient_id,a.appointment_no,a.package_name,p.name,p.sex,p.age,p.phone,p.avatar"+scheduleJoin;}
    /**
     * 按日、周或月查询日程、容量统计和未处理异常。
     * @param q 查询字符串参数
     * @return 包含查询结果或业务状态的键值数据
     * @throws IllegalArgumentException 当参数格式错误、记录不存在或不满足业务规则时抛出
     */
    public Map<String,Object> schedule(Map<String,String>q){
        LocalDate date=LocalDate.parse(q.getOrDefault("date",LocalDate.now().toString()));String view=q.getOrDefault("view","day");LocalDate from=date,to=date;
        if(view.equals("week")){from=date.minusDays(date.getDayOfWeek().getValue()-1);to=from.plusDays(6);}else if(view.equals("month")){from=date.withDayOfMonth(1);to=from.plusMonths(1).minusDays(1);}else if(!view.equals("day"))throw new IllegalArgumentException("日历视图无效");
        Map<String,Object>r=new LinkedHashMap<>();r.put("events",rows(scheduleSelect()+" WHERE s.schedule_date BETWEEN ? AND ? AND s.status<>'已取消' AND p.deleted=0 ORDER BY s.schedule_date,s.start_time,s.id",from.toString(),to.toString()));
        r.put("calendar",rows("SELECT schedule_date date,COUNT(*) count,SUM(status='已完成') completed FROM doctor_schedule WHERE schedule_date BETWEEN ? AND ? AND status<>'已取消' GROUP BY schedule_date",date.withDayOfMonth(1).toString(),date.withDayOfMonth(1).plusMonths(1).minusDays(1).toString()));
        r.put("stats",one("SELECT COUNT(*) total,COALESCE(SUM(status IN ('已预约','待检')),0) pending,COALESCE(SUM(status='已完成'),0) completed,COALESCE(SUM(status='进行中'),0) in_progress FROM doctor_schedule WHERE schedule_date=? AND status<>'已取消'",date.toString()));
        r.put("previous",one("SELECT COUNT(*) total,COALESCE(SUM(status IN ('已预约','待检')),0) pending,COALESCE(SUM(status='已完成'),0) completed FROM doctor_schedule WHERE schedule_date=? AND status<>'已取消'",date.minusDays(1).toString()));
        r.put("alerts",rows("SELECT a.*,p.name,p.sex,p.age FROM doctor_alert a JOIN doctor_patient p ON p.id=a.patient_id WHERE a.resolved=0 ORDER BY a.created_at DESC LIMIT 30"));r.put("rules",rules());r.put("date",date.toString());return r;
    }
    /**
     * 锁定指定排班日期，使同一天的并发容量检查串行执行。
     * @param date 目标日期
     */
    private void lockDate(String date){db.update("INSERT IGNORE INTO doctor_schedule_day(schedule_date) VALUES(?)",date);one("SELECT schedule_date FROM doctor_schedule_day WHERE schedule_date=? FOR UPDATE",date);}
    /**
     * 校验套餐有效性、患者性别、日期容量和诊室时段冲突。
     * @param patientId patientId 参数
     * @param date 目标日期
     * @param slot 预约时间段
     * @param packageName 体检套餐名称
     * @throws IllegalArgumentException 当参数格式错误、记录不存在或不满足业务规则时抛出
     */
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
    /**
     * 校验预约日期、工作时段和预约时长。
     * @param date 目标日期
     * @param start 时间段开始时间
     * @param end 时间段结束时间
     * @throws IllegalArgumentException 当参数格式错误、记录不存在或不满足业务规则时抛出
     */
    private void validateHours(LocalDate date,LocalTime start,LocalTime end){
        Map<String,Object> r=rules();boolean workday=false;for(Object day:(List<?>)r.get("workdays"))if(number(day,"工作日")==date.getDayOfWeek().getValue())workday=true;
        if(!workday)throw new IllegalArgumentException("所选日期为非工作日");
        LocalTime ms=LocalTime.parse(r.get("morningStart").toString()),me=LocalTime.parse(r.get("morningEnd").toString()),as=LocalTime.parse(r.get("afternoonStart").toString()),ae=LocalTime.parse(r.get("afternoonEnd").toString());
        boolean morning=!start.isBefore(ms)&&!end.isAfter(me),afternoon=!start.isBefore(as)&&!end.isAfter(ae);
        if(!start.isBefore(end)||!(morning||afternoon))throw new IllegalArgumentException("预约时间应在工作时段内，且结束时间晚于开始时间");
        int minutes=number(r.get("slotMinutes"),"时段长度");if(Duration.between(start,end).toMinutes()%minutes!=0)throw new IllegalArgumentException("预约时长须为"+minutes+"分钟的整数倍");
    }
    /**
     * 为医生端预约创建对应的日程记录。
     * @param aid 预约或异常提醒主键
     */
    public void attachSchedule(long aid){Map<String,Object>a=one("SELECT * FROM doctor_appointment WHERE id=?",aid);String[]time=a.get("timeSlot").toString().split("–");String room=text(object(settings().get("preferences")),"defaultRoom","体检一室");db.update("INSERT IGNORE INTO doctor_schedule(appointment_id,schedule_date,start_time,end_time,room) VALUES(?,?,?,?,?)",aid,a.get("examDate"),time[0],time[1],room);}
    /**
     * 将预约状态同步到关联日程。
     * @param aid 预约或异常提醒主键
     * @param status 目标业务状态
     */
    public void syncAppointmentStatus(long aid,String status){db.update("UPDATE doctor_schedule SET status=?,version=version+1 WHERE appointment_id=?",status.equals("已完成")?"已完成":status.equals("已取消")?"已取消":"已预约",aid);}
    /**
     * 查询指定日程及其患者和预约信息。
     * @param sid 日程主键
     * @return 包含查询结果或业务状态的键值数据
     */
    private Map<String,Object> scheduleEvent(long sid){return one(scheduleSelect()+" WHERE s.id=?",sid);}
    /**
     * 在校验版本、工作时间和冲突后调整单条日程。
     * @param sid 日程主键
     * @param b 请求体键值数据
     * @return 包含查询结果或业务状态的键值数据
     * @throws IllegalArgumentException 当参数格式错误、记录不存在或不满足业务规则时抛出
     */
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
    /**
     * 检查目标诊室时段、患者重复预约和日期容量。
     * @param sid 日程主键
     * @param aid 预约或异常提醒主键
     * @param date 目标日期
     * @param start 时间段开始时间
     * @param end 时间段结束时间
     * @param room 诊室名称
     * @param excluded excluded 参数
     * @throws IllegalArgumentException 当参数格式错误、记录不存在或不满足业务规则时抛出
     */
    private void checkSlot(long sid,long aid,String date,LocalTime start,LocalTime end,String room,List<Long>excluded){
        String placeholders=String.join(",",Collections.nCopies(excluded.size(),"?"));List<Object>args=new ArrayList<>(Arrays.asList(date,room,end.toString(),start.toString()));args.addAll(excluded);
        if(count("SELECT COUNT(*) FROM doctor_schedule WHERE schedule_date=? AND room=? AND start_time<? AND end_time>? AND status<>'已取消' AND id NOT IN ("+placeholders+")",args.toArray())>0)throw new IllegalArgumentException("目标诊室在该时段已有安排，请更换时段或诊室");
        Map<String,Object>a=one("SELECT patient_id,exam_date FROM doctor_appointment WHERE id=?",aid);
        if(count("SELECT COUNT(*) FROM doctor_appointment WHERE patient_id=? AND exam_date=? AND id<>? AND status<>'已取消'",a.get("patientId"),date,aid)>0)throw new IllegalArgumentException("患者在目标日期已有预约");
        if(!date.equals(a.get("examDate"))&&count("SELECT COUNT(*) FROM doctor_appointment WHERE exam_date=? AND status<>'已取消'",date)>=number(rules().get("dailyCapacity"),"每日容量"))throw new IllegalArgumentException("目标日期预约已满");
    }
    /**
     * 原子化调整多条日程，并检查容量、患者重复和诊室冲突。
     * @param b 请求体键值数据
     * @return 处理结果数值
     * @throws IllegalArgumentException 当参数格式错误、记录不存在或不满足业务规则时抛出
     */
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
