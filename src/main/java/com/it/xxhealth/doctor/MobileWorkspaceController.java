package com.it.xxhealth.doctor;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 移动用户工作区接口，提供医生端套餐、预约、报告和消息的移动端视图。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
@RestController
@RequestMapping("/mobile/workspace")
public class MobileWorkspaceController {
    /**
     * 当前控制器使用的业务服务。
     */
    private final MobileWorkspaceService service;

    /**
     * 创建 MobileWorkspaceController 实例并注入所需依赖。
     * @param service 当前控制器对应的业务服务
     */
    public MobileWorkspaceController(MobileWorkspaceService service) {
        this.service = service;
    }

    /**
     * 查询当前可见的体检套餐，并按筛选条件返回分页或移动端列表。
     * @return 符合条件的数据集合
     */
    @PostMapping("/packages")
    public List<Map<String, Object>> packages() {
        return service.packages();
    }

    /**
     * 查询指定的已上架体检套餐详情。
     * @param body 请求体键值数据
     * @return 包含查询结果或业务状态的键值数据
     */
    @PostMapping("/packages/detail")
    public Map<String, Object> packageDetail(@RequestBody Map<String, Object> body) {
        return service.packageDetail(number(body, "packageid"));
    }

    /**
     * 校验用户、机构、套餐适用性和日期后创建移动端预约。
     * @param body 请求体键值数据
     * @return 包含查询结果或业务状态的键值数据
     * @throws IllegalArgumentException 当参数格式错误、记录不存在或不满足业务规则时抛出
     */
    @PostMapping("/book")
    public Map<String, Object> book(@RequestBody Map<String, Object> body) {
        Object hospitalId = body.get("hpid");
        Object orderDate = body.get("orderdate");
        if (hospitalId == null || orderDate == null) {
            throw new IllegalArgumentException("体检机构和预约日期不能为空");
        }
        return service.book(userId(body), number(body, "packageid"),
                (int) number(body, "hpid"), orderDate.toString());
    }

    /**
     * 按患者、状态、套餐和日期分页查询医生端预约。
     * @param body 请求体键值数据
     * @return 符合条件的数据集合
     */
    @PostMapping("/appointments")
    public List<Map<String, Object>> appointments(@RequestBody Map<String, Object> body) {
        return service.appointments(userId(body));
    }

    /**
     * 按患者、状态、套餐和日期范围分页查询体检报告。
     * @param body 请求体键值数据
     * @return 符合条件的数据集合
     */
    @PostMapping("/reports")
    public List<Map<String, Object>> reports(@RequestBody Map<String, Object> body) {
        return service.reports(userId(body));
    }

    /**
     * 查询报告详情、检查结果和患者历史报告。
     * @param body 请求体键值数据
     * @return 包含查询结果或业务状态的键值数据
     * @throws IllegalArgumentException 当参数格式错误、记录不存在或不满足业务规则时抛出
     */
    @PostMapping("/reports/detail")
    public Map<String, Object> report(@RequestBody Map<String, Object> body) {
        Object reportId = body.get("reportid");
        if (reportId == null) {
            throw new IllegalArgumentException("报告编号不能为空");
        }
        try {
            return service.report(userId(body), Long.parseLong(reportId.toString()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("报告编号格式错误");
        }
    }

    /**
     * 汇总用户的预约、报告、异常数量和下一次预约。
     * @param body 请求体键值数据
     * @return 包含查询结果或业务状态的键值数据
     */
    @PostMapping("/summary")
    public Map<String, Object> summary(@RequestBody Map<String, Object> body) {
        return service.summary(userId(body));
    }

    /**
     * 聚合用户的预约状态和报告发布事件，生成时间倒序消息。
     * @param body 请求体键值数据
     * @return 符合条件的数据集合
     */
    @PostMapping("/messages")
    public List<Map<String, Object>> messages(@RequestBody Map<String, Object> body) {
        return service.messages(userId(body));
    }

    /**
     * 从请求对象中读取并规范化用户手机号。
     * @param body 请求体键值数据
     * @return 方法处理结果
     */
    private String userId(Map<String, Object> body) {
        Object value = body.get("userid");
        return value == null ? "" : value.toString().trim();
    }

    /**
     * 将输入值转换为整数，并提供统一的格式错误提示。
     * @param body 请求体键值数据
     * @param key 请求字段名
     * @return 处理结果数值
     * @throws IllegalArgumentException 当参数格式错误、记录不存在或不满足业务规则时抛出
     */
    private long number(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null) {
            throw new IllegalArgumentException(key + "不能为空");
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(key + "格式错误");
        }
    }
}
