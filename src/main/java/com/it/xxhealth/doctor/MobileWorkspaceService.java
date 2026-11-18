package com.it.xxhealth.doctor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.it.xxhealth.entity.Orders;
import com.it.xxhealth.service.OrdersService;
import java.sql.ResultSetMetaData;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Service;

/**
 * 移动用户工作区查询服务，组合旧用户数据与医生端业务数据。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
@Service
public class MobileWorkspaceService {
    /**
     * 执行参数化 SQL 和事务内数据库操作的 JdbcTemplate。
     */
    private final JdbcTemplate db;
    /**
     * 负责 Java 对象与 JSON 字符串互转的序列化组件。
     */
    private final ObjectMapper json;
    /**
     * 预约订单业务服务。
     */
    private final OrdersService ordersService;

    /**
     * 创建 MobileWorkspaceService 实例并注入所需依赖。
     * @param db 数据库访问组件
     * @param json JSON 序列化组件
     * @param ordersService ordersService 参数
     */
    public MobileWorkspaceService(JdbcTemplate db, ObjectMapper json, OrdersService ordersService) {
        this.db = db;
        this.json = json;
        this.ordersService = ordersService;
    }

    /**
     * 查询当前可见的体检套餐，并按筛选条件返回分页或移动端列表。
     * @return 符合条件的数据集合
     */
    public List<Map<String, Object>> packages() {
        List<Map<String, Object>> packages = rows(
                "SELECT id package_id,name,category,audience,gender,price,original_price,features,items,picture "
                        + "FROM doctor_package WHERE enabled=1 ORDER BY sort_order,id");
        for (Map<String, Object> item : packages) {
            item.put("features", decodeList(item.get("features")));
            item.put("items", decodeList(item.get("items")));
        }
        return packages;
    }

    /**
     * 查询指定的已上架体检套餐详情。
     * @param packageId 医生端套餐编号
     * @return 包含查询结果或业务状态的键值数据
     * @throws IllegalArgumentException 当参数格式错误、记录不存在或不满足业务规则时抛出
     */
    public Map<String, Object> packageDetail(long packageId) {
        List<Map<String, Object>> found = rows(
                "SELECT id package_id,name,category,audience,gender,price,original_price,features,items,picture "
                        + "FROM doctor_package WHERE id=? AND enabled=1", packageId);
        if (found.isEmpty()) {
            throw new IllegalArgumentException("套餐不存在或已下架");
        }
        Map<String, Object> result = found.get(0);
        result.put("features", decodeList(result.get("features")));
        result.put("items", decodeList(result.get("items")));
        return result;
    }

    /**
     * 校验用户、机构、套餐适用性和日期后创建移动端预约。
     * @param userId 用户手机号
     * @param packageId 医生端套餐编号
     * @param hospitalId 体检机构编号
     * @param orderDate orderDate 参数
     * @return 包含查询结果或业务状态的键值数据
     * @throws IllegalArgumentException 当参数格式错误、记录不存在或不满足业务规则时抛出
     */
    public Map<String, Object> book(String userId, long packageId, int hospitalId, String orderDate) {
        requiredUser(userId);
        LocalDate.parse(orderDate);
        Map<String, Object> pack = packageDetail(packageId);
        Long hospitalCount = db.queryForObject("SELECT COUNT(*) FROM hospital WHERE hpId=? AND state=1", Long.class, hospitalId);
        if (hospitalCount == null || hospitalCount == 0) {
            throw new IllegalArgumentException("体检机构不存在或暂停营业");
        }
        Integer sex = db.queryForObject("SELECT sex FROM users WHERE userId=?", Integer.class, userId);
        String gender = String.valueOf(pack.get("gender"));
        if ("男".equals(gender) && (sex == null || sex != 1)) {
            throw new IllegalArgumentException("该套餐仅适用于男性");
        }
        if ("女".equals(gender) && (sex == null || sex != 0)) {
            throw new IllegalArgumentException("该套餐仅适用于女性");
        }
        List<Integer> legacyIds = db.query("SELECT smId FROM setmeal WHERE name=? ORDER BY smId LIMIT 1",
                (rs, rowNum) -> rs.getInt(1), pack.get("name"));
        Integer setmealId;
        if (legacyIds.isEmpty()) {
            int type = "女".equals(gender) ? 0 : 1;
            db.update("INSERT INTO setmeal(name,type,price) VALUES(?,?,?)", pack.get("name"), type,
                    ((Number) pack.get("price")).intValue());
            setmealId = db.queryForObject("SELECT smId FROM setmeal WHERE name=? ORDER BY smId DESC LIMIT 1",
                    Integer.class, pack.get("name"));
        } else {
            setmealId = legacyIds.get(0);
        }
        Orders order = new Orders();
        order.setUserid(userId);
        order.setHpid(hospitalId);
        order.setSmid(setmealId);
        order.setOrderdate(java.sql.Date.valueOf(orderDate));
        int result = ordersService.saveOrder(order);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("result", result);
        response.put("orderId", order.getOrderid());
        response.put("packageName", pack.get("name"));
        response.put("price", pack.get("price"));
        return response;
    }

    /**
     * 按患者、状态、套餐和日期分页查询医生端预约。
     * @param userId 用户手机号
     * @return 符合条件的数据集合
     */
    public List<Map<String, Object>> appointments(String userId) {
        requiredUser(userId);
        return rows("SELECT o.orderId order_id,o.orderDate order_date,o.userId user_id,o.hpId hp_id,o.smId sm_id,o.state,"
                        + "h.name hospital_name,s.name setmeal_name,s.price,"
                        + "a.id appointment_id,a.appointment_no appointment_no,"
                        + "COALESCE(a.status,CASE WHEN o.state=2 THEN '已完成' WHEN o.state=3 THEN '已取消' ELSE '待确认' END) doctor_status,"
                        + "a.time_slot time_slot,a.source,ds.room,ds.notes,a.created_at created_at "
                        + "FROM orders o JOIN hospital h ON h.hpId=o.hpId JOIN setmeal s ON s.smId=o.smId "
                        + "LEFT JOIN doctor_appointment a ON a.appointment_no=CONCAT('MOB',LPAD(o.orderId,10,'0')) "
                        + "LEFT JOIN doctor_schedule ds ON ds.appointment_id=a.id "
                        + "WHERE o.userId=? ORDER BY o.orderDate DESC,o.orderId DESC",
                userId);
    }

    /**
     * 按患者、状态、套餐和日期范围分页查询体检报告。
     * @param userId 用户手机号
     * @return 符合条件的数据集合
     */
    public List<Map<String, Object>> reports(String userId) {
        requiredUser(userId);
        return rows("SELECT r.id report_id,r.report_no report_no,r.exam_date exam_date,r.package_name package_name,"
                        + "r.status,r.score,r.conclusion,r.advice,r.doctor_name doctor_name,r.reported_at reported_at,"
                        + "COALESCE(SUM(a.resolved=0),0) unresolved_alerts "
                        + "FROM doctor_report r JOIN doctor_patient p ON p.id=r.patient_id "
                        + "LEFT JOIN doctor_alert a ON a.report_id=r.id "
                        + "WHERE p.phone=? AND p.deleted=0 AND r.status='已生成' "
                        + "GROUP BY r.id ORDER BY r.reported_at DESC,r.id DESC",
                userId);
    }

    /**
     * 查询报告详情、检查结果和患者历史报告。
     * @param userId 用户手机号
     * @param reportId 体检报告编号
     * @return 包含查询结果或业务状态的键值数据
     * @throws IllegalArgumentException 当参数格式错误、记录不存在或不满足业务规则时抛出
     */
    public Map<String, Object> report(String userId, long reportId) {
        requiredUser(userId);
        List<Map<String, Object>> found = rows(
                "SELECT r.id report_id,r.report_no report_no,r.exam_date exam_date,r.package_name package_name,"
                        + "r.status,r.score,r.conclusion,r.advice,r.items,r.doctor_name doctor_name,r.reported_at reported_at "
                        + "FROM doctor_report r JOIN doctor_patient p ON p.id=r.patient_id "
                        + "WHERE r.id=? AND p.phone=? AND p.deleted=0 AND r.status='已生成'",
                reportId, userId);
        if (found.isEmpty()) {
            throw new IllegalArgumentException("报告不存在或尚未发布");
        }
        Map<String, Object> result = found.get(0);
        result.put("items", decodeItems(result.get("items")));
        result.put("alerts", rows("SELECT type,indicator,resolved FROM doctor_alert WHERE report_id=? ORDER BY id", reportId));
        return result;
    }

    /**
     * 汇总用户的预约、报告、异常数量和下一次预约。
     * @param userId 用户手机号
     * @return 包含查询结果或业务状态的键值数据
     */
    public Map<String, Object> summary(String userId) {
        List<Map<String, Object>> appointmentList = appointments(userId);
        List<Map<String, Object>> reportList = reports(userId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("appointments", appointmentList.size());
        result.put("pending", appointmentList.stream()
                .filter(item -> !"已完成".equals(item.get("doctorStatus")) && !"已取消".equals(item.get("doctorStatus")))
                .count());
        result.put("reports", reportList.size());
        result.put("alerts", reportList.stream().mapToLong(item -> number(item.get("unresolvedAlerts"))).sum());
        result.put("nextAppointment", appointmentList.stream()
                .filter(item -> !"已完成".equals(item.get("doctorStatus")) && !"已取消".equals(item.get("doctorStatus")))
                .findFirst().orElse(null));
        return result;
    }

    /**
     * 聚合用户的预约状态和报告发布事件，生成时间倒序消息。
     * @param userId 用户手机号
     * @return 符合条件的数据集合
     */
    public List<Map<String, Object>> messages(String userId) {
        List<Map<String, Object>> messages = new ArrayList<>();
        for (Map<String, Object> appointment : appointments(userId)) {
            Map<String, Object> message = new LinkedHashMap<>();
            message.put("type", "appointment");
            message.put("title", "预约" + appointment.get("doctorStatus"));
            message.put("content", appointment.get("setmealName") + "，" + appointment.get("orderDate")
                    + (appointment.get("timeSlot") == null ? "" : " " + appointment.get("timeSlot")));
            message.put("time", appointment.get("createdAt"));
            message.put("appointmentId", appointment.get("appointmentId"));
            messages.add(message);
        }
        for (Map<String, Object> report : reports(userId)) {
            Map<String, Object> message = new LinkedHashMap<>();
            message.put("type", "report");
            message.put("title", "体检报告已发布");
            message.put("content", report.get("packageName") + "，健康评分 " + report.get("score"));
            message.put("time", report.get("reportedAt"));
            message.put("reportId", report.get("reportId"));
            messages.add(message);
        }
        messages.sort(Comparator.comparing(item -> String.valueOf(item.get("time")), Comparator.reverseOrder()));
        return messages;
    }

    /**
     * 执行 requiredUser 对应的业务处理。
     * @param userId 用户手机号
     * @throws IllegalArgumentException 当参数格式错误、记录不存在或不满足业务规则时抛出
     */
    private void requiredUser(String userId) {
        // 账号注册与旧版用户表都以“11 位数字”作为用户编号。这里不能额外
        // 限制号段，否则数据库中已有的演示账号（如 12300000000）可以登录，
        // 却会在预约、报告和消息接口中被错误拒绝。
        if (userId == null || !userId.matches("\\d{11}")) {
            throw new IllegalArgumentException("用户编号格式错误");
        }
        Long count = db.queryForObject("SELECT COUNT(*) FROM users WHERE userId=?", Long.class, userId);
        if (count == null || count == 0) {
            throw new IllegalArgumentException("用户不存在");
        }
    }

    /**
     * 执行参数化查询，并将结果集转换为驼峰命名的键值集合。
     * @param sql 包含占位符的 SQL 语句
     * @param args 应用启动参数或 SQL 绑定参数
     * @return 符合条件的数据集合
     */
    private List<Map<String, Object>> rows(String sql, Object... args) {
        return db.query(sql, (rs, rowNum) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            ResultSetMetaData meta = rs.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                Object value = rs.getObject(i);
                if (value instanceof java.sql.Date || value instanceof Time || value instanceof Timestamp) {
                    value = value.toString();
                }
                row.put(JdbcUtils.convertUnderscoreNameToPropertyName(meta.getColumnLabel(i)), value);
            }
            return row;
        }, args);
    }

    /**
     * 解析报告检查项目 JSON。
     * @param value 待转换或校验的输入值
     * @return 符合条件的数据集合
     * @throws IllegalArgumentException 当参数格式错误、记录不存在或不满足业务规则时抛出
     */
    private List<Map<String, Object>> decodeItems(Object value) {
        try {
            return json.readValue(String.valueOf(value), new TypeReference<List<Map<String, Object>>>() { });
        } catch (Exception e) {
            throw new IllegalArgumentException("报告检查项数据格式错误");
        }
    }

    /**
     * 解析套餐标签或项目 JSON 数组。
     * @param value 待转换或校验的输入值
     * @return 符合条件的数据集合
     */
    private List<Object> decodeList(Object value) {
        try {
            return json.readValue(String.valueOf(value), new TypeReference<List<Object>>() { });
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 将输入值转换为整数，并提供统一的格式错误提示。
     * @param value 待转换或校验的输入值
     * @return 处理结果数值
     */
    private long number(Object value) {
        return value == null ? 0 : Long.parseLong(value.toString());
    }
}
