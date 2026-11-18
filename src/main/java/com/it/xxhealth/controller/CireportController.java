package com.it.xxhealth.controller;

import com.it.xxhealth.common.Result;
import com.it.xxhealth.dto.CiReportDto;
import com.it.xxhealth.entity.Cireport;
import com.it.xxhealth.entity.Orders;
import com.it.xxhealth.service.CireportService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 体检报告检查项接口，负责生成报告项目并查询检查明细。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
@RestController
@RequestMapping("/ciReport")
public class CireportController {

    /**
     * 报告检查项目业务服务。
     */
    @Resource
    private CireportService cireportService;

    /**
     * 根据订单套餐复制检查项和明细，生成报告录入模板。
     * @param orders 预约订单数据
     * @return 保存、更新或同步后的业务结果
     */
    @PostMapping("/createReportTemplate")
    public Result<Integer> createReportTemplate(@RequestBody Orders orders) {
        int count = cireportService.createReportTemplate(orders);
        return Result.success(count);
    }

    /**
     * 查询指定订单的报告检查项目及结果明细。
     * @param cireport 报告检查项目查询条件
     * @return 符合条件的数据集合
     */
    @PostMapping("/listCiReport")
    public List<CiReportDto> listCiReport(@RequestBody Cireport cireport) {
        return cireportService.listCiReport(cireport);
    }
}
