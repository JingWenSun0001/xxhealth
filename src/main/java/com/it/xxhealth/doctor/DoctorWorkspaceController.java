package com.it.xxhealth.doctor;

import com.it.xxhealth.common.Result;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * 医生工作台核心接口，提供患者、预约、报告和异常提醒功能。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
@RestController
@RequestMapping("/doctor/workspace")
public class DoctorWorkspaceController {
    /**
     * 当前控制器使用的业务服务。
     */
    private final DoctorWorkspaceService service;
    /**
     * 创建 DoctorWorkspaceController 实例并注入所需依赖。
     * @param service 当前控制器对应的业务服务
     */
    public DoctorWorkspaceController(DoctorWorkspaceService service) { this.service = service; }
    /**
     * 汇总医生工作台统计、待办、异常提醒和七日趋势。
     * @return 方法处理结果
     */
    @GetMapping("/dashboard") public Result<?> dashboard() { return Result.success(service.dashboard()); }
    /**
     * 按患者信息、健康状态和体检日期分页查询患者档案。
     * @param q 查询字符串参数
     * @return 方法处理结果
     */
    @GetMapping("/patients") public Result<?> patients(@RequestParam Map<String,String> q) { return Result.success(service.patients(q)); }
    /**
     * 查询指定的有效患者档案。
     * @param id 业务主键
     * @return 方法处理结果
     */
    @GetMapping("/patients/{id}") public Result<?> patient(@PathVariable Long id) { return Result.success(service.patient(id)); }
    /**
     * 校验患者、套餐、容量和时段后创建预约、日程及通知。
     * @param body 请求体键值数据
     * @return 保存、更新或同步后的业务结果
     */
    @PostMapping("/patients") public Result<?> createPatient(@RequestBody Map<String,Object> body) { return Result.success(service.savePatient(null, body)); }
    /**
     * 校验输入并更新业务记录。
     * @param id 业务主键
     * @param body 请求体键值数据
     * @return 保存、更新或同步后的业务结果
     */
    @PutMapping("/patients/{id}") public Result<?> updatePatient(@PathVariable Long id,@RequestBody Map<String,Object> body) { return Result.success(service.savePatient(id, body)); }
    /**
     * 在确认没有未完成预约后软删除患者档案。
     * @param id 业务主键
     * @return 保存、更新或同步后的业务结果
     */
    @DeleteMapping("/patients/{id}") public Result<?> deletePatient(@PathVariable Long id) { service.deletePatient(id); return Result.success(); }
    /**
     * 在同一事务中批量校验并导入患者档案。
     * @param body 请求体键值数据
     * @return 方法处理结果
     */
    @PostMapping("/patients/import") public Result<?> importPatients(@RequestBody List<Map<String,Object>> body) { return Result.success(service.importPatients(body)); }
    /**
     * 按患者、状态、套餐和日期分页查询医生端预约。
     * @param q 查询字符串参数
     * @return 方法处理结果
     */
    @GetMapping("/appointments") public Result<?> appointments(@RequestParam Map<String,String> q) { return Result.success(service.appointments(q)); }
    /**
     * 查询指定医生端预约及其患者信息。
     * @param id 业务主键
     * @return 方法处理结果
     */
    @GetMapping("/appointments/{id}") public Result<?> appointment(@PathVariable Long id) { return Result.success(service.appointment(id)); }
    /**
     * 校验输入并创建业务记录。
     * @param body 请求体键值数据
     * @return 保存、更新或同步后的业务结果
     */
    @PostMapping("/appointments") public Result<?> createAppointment(@RequestBody Map<String,Object> body) { return Result.success(service.createAppointment(body)); }
    /**
     * 按照预约状态机更新预约，并同步日程和用户端订单。
     * @param id 业务主键
     * @param body 请求体键值数据
     * @return 方法处理结果
     */
    @PutMapping("/appointments/{id}/status") public Result<?> appointmentStatus(@PathVariable Long id,@RequestBody Map<String,Object> body) { return Result.success(service.appointmentStatus(id, body)); }
    /**
     * 统计指定月份每日的未取消预约数量。
     * @param month 目标月份
     * @return 方法处理结果
     */
    @GetMapping("/calendar") public Result<?> calendar(@RequestParam String month) { return Result.success(service.calendar(month)); }
    /**
     * 按患者、状态、套餐和日期范围分页查询体检报告。
     * @param q 查询字符串参数
     * @return 方法处理结果
     */
    @GetMapping("/reports") public Result<?> reports(@RequestParam Map<String,String> q) { return Result.success(service.reports(q)); }
    /**
     * 查询报告详情、检查结果和患者历史报告。
     * @param id 业务主键
     * @return 方法处理结果
     */
    @GetMapping("/reports/{id}") public Result<?> report(@PathVariable Long id) { return Result.success(service.report(id)); }
    /**
     * 根据患者和报告模板创建一份草稿报告。
     * @param body 请求体键值数据
     * @return 保存、更新或同步后的业务结果
     */
    @PostMapping("/reports") public Result<?> createReport(@RequestBody Map<String,Object> body) { return Result.success(service.createReport(body)); }
    /**
     * 校验检查结果和健康评分后保存可编辑报告。
     * @param id 业务主键
     * @param body 请求体键值数据
     * @return 保存、更新或同步后的业务结果
     */
    @PutMapping("/reports/{id}") public Result<?> saveReport(@PathVariable Long id,@RequestBody Map<String,Object> body) { return Result.success(service.saveReport(id, body)); }
    /**
     * 按照报告状态机提交、退回或发布报告，并处理异常联动。
     * @param id 业务主键
     * @param body 请求体键值数据
     * @return 方法处理结果
     */
    @PutMapping("/reports/{id}/status") public Result<?> reportStatus(@PathVariable Long id,@RequestBody Map<String,Object> body) { return Result.success(service.reportStatus(id, body)); }
    /**
     * 查询全部报告模板并解析模板检查项。
     * @return 方法处理结果
     */
    @GetMapping("/templates") public Result<?> templates() { return Result.success(service.templates()); }
    /**
     * 校验检查项后创建自定义报告模板。
     * @param body 请求体键值数据
     * @return 保存、更新或同步后的业务结果
     */
    @PostMapping("/templates") public Result<?> createTemplate(@RequestBody Map<String,Object> body) { return Result.success(service.createTemplate(body)); }
    /**
     * 按患者和异常类型分页查询异常提醒。
     * @param q 查询字符串参数
     * @return 方法处理结果
     */
    @GetMapping("/alerts") public Result<?> alerts(@RequestParam Map<String,String> q) { return Result.success(service.alerts(q)); }
    /**
     * 标记异常提醒为已处理，并按剩余异常更新患者随访状态。
     * @param id 业务主键
     * @return 方法处理结果
     */
    @PutMapping("/alerts/{id}") public Result<?> resolveAlert(@PathVariable Long id) { service.resolveAlert(id); return Result.success(); }
}
