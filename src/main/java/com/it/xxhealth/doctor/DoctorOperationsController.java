package com.it.xxhealth.doctor;

import com.it.xxhealth.common.Result;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * 医生端运营接口，提供套餐、日程、消息和设置管理功能。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
@RestController
@RequestMapping("/doctor/workspace")
public class DoctorOperationsController {
    /**
     * 当前控制器使用的业务服务。
     */
    private final DoctorOperationsService service;
    /**
     * 用户端历史订单补同步服务。
     */
    private final MobileDoctorSyncService mobileSync;
    /**
     * 创建 DoctorOperationsController 实例并注入所需依赖。
     * @param service 当前控制器对应的业务服务
     * @param mobileSync 用户端订单同步服务
     */
    public DoctorOperationsController(DoctorOperationsService service,MobileDoctorSyncService mobileSync){this.service=service;this.mobileSync=mobileSync;}
    /**
     * 查询当前可见的体检套餐，并按筛选条件返回分页或移动端列表。
     * @param q 查询字符串参数
     * @return 方法处理结果
     */
    @GetMapping("/packages") public Result<?> packages(@RequestParam Map<String,String>q){return Result.success(service.packages(q));}
    /**
     * 查询指定的已上架体检套餐详情。
     * @param id 业务主键
     * @return 方法处理结果
     */
    @GetMapping("/packages/{id}") public Result<?> packageDetail(@PathVariable long id){return Result.success(service.packageDetail(id));}
    /**
     * 校验输入并创建业务记录。
     * @param b 请求体键值数据
     * @return 保存、更新或同步后的业务结果
     */
    @PostMapping("/packages") public Result<?> createPackage(@RequestBody Map<String,Object>b){return Result.success(service.savePackage(null,b));}
    /**
     * 校验套餐价格、标签、检查项和版本号后新增或更新套餐。
     * @param id 业务主键
     * @param b 请求体键值数据
     * @return 保存、更新或同步后的业务结果
     */
    @PutMapping("/packages/{id}") public Result<?> savePackage(@PathVariable long id,@RequestBody Map<String,Object>b){return Result.success(service.savePackage(id,b));}
    /**
     * 校验版本号并更新套餐上架状态。
     * @param id 业务主键
     * @param b 请求体键值数据
     * @return 方法处理结果
     */
    @PutMapping("/packages/{id}/status") public Result<?> packageStatus(@PathVariable long id,@RequestBody Map<String,Object>b){return Result.success(service.packageStatus(id,b));}
    /**
     * 按日、周或月查询日程、容量统计和未处理异常。
     * @param q 查询字符串参数
     * @return 方法处理结果
     */
    @GetMapping("/schedule") public Result<?> schedule(@RequestParam Map<String,String>q){return Result.success(service.schedule(q));}
    /**
     * 执行 batch 对应的业务处理。
     * @param b 请求体键值数据
     * @return 方法处理结果
     */
    @PutMapping("/schedule/batch") public Result<?> batch(@RequestBody Map<String,Object>b){return Result.success(service.batchSchedule(b));}
    /**
     * 执行 scheduleUpdate 对应的业务处理。
     * @param id 业务主键
     * @param b 请求体键值数据
     * @return 方法处理结果
     */
    @PutMapping("/schedule/{id}") public Result<?> scheduleUpdate(@PathVariable long id,@RequestBody Map<String,Object>b){return Result.success(service.updateSchedule(id,b));}
    /**
     * 按分类、状态、关键字和时间范围分页查询消息。
     * @param q 查询字符串参数
     * @return 方法处理结果
     */
    @GetMapping("/notifications") public Result<?> notifications(@RequestParam Map<String,String>q){return Result.success(service.notifications(q));}
    /**
     * 汇总用户的预约、报告、异常数量和下一次预约。
     * @return 方法处理结果
     */
    @GetMapping("/notifications/summary") public Result<?> summary(){return Result.success(service.notificationSummary());}
    /**
     * 查询指定消息的完整内容。
     * @param id 业务主键
     * @return 方法处理结果
     */
    @GetMapping("/notifications/{id}") public Result<?> notification(@PathVariable long id){return Result.success(service.notification(id));}
    /**
     * 将所有未读消息批量标记为已读。
     * @return 方法处理结果
     */
    @PutMapping("/notifications/read-all") public Result<?> readAll(){return Result.success(service.readAll());}
    /**
     * 执行 read 对应的业务处理。
     * @param id 业务主键
     * @param b 请求体键值数据
     * @return 方法处理结果
     */
    @PutMapping("/notifications/{id}/read") public Result<?> read(@PathVariable long id,@RequestBody Map<String,Object>b){service.readNotification(id,b);return Result.success();}
    /**
     * 读取医生工作台的个人资料、偏好、通知和排班规则。
     * @return 方法处理结果
     */
    @GetMapping("/settings") public Result<?> settings(){return Result.success(service.settings());}
    /**
     * 按设置分区合并、校验并持久化工作台配置。
     * @param section 系统设置分区名称
     * @param b 请求体键值数据
     * @return 保存、更新或同步后的业务结果
     */
    @PutMapping("/settings/{section}") public Result<?> saveSettings(@PathVariable String section,@RequestBody Map<String,Object>b){return Result.success(service.saveSettings(section,b));}
    /**
     * 执行 logs 对应的业务处理。
     * @param q 查询字符串参数
     * @return 方法处理结果
     */
    @GetMapping("/operation-logs") public Result<?> logs(@RequestParam Map<String,String>q){return Result.success(service.auditLogs(q));}
    /**
     * 执行 mobileSync 对应的业务处理。
     * @return 方法处理结果
     */
    @PostMapping("/mobile-sync") public Result<?> mobileSync(){return Result.success(mobileSync.syncAllOrders());}
}
