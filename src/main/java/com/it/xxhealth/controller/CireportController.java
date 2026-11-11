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
 * 医生端：体检报告检查项接口
 */
@RestController
@RequestMapping("/ciReport")
public class CireportController {

    @Resource
    private CireportService cireportService;

    /**
     * 根据预约订单信息创建体检报告模板
     *
     * @param orders 预约订单对象（只需 orderid）
     * @return 本次创建的检查项数量
     */
    @PostMapping("/createReportTemplate")
    public Result<Integer> createReportTemplate(@RequestBody Orders orders) {
        int count = cireportService.createReportTemplate(orders);
        return Result.success(count);
    }

    /**
     * 根据体检预约编号查询检查项列表及明细信息
     *
     * @param cireport 检查项报告对象（只需 orderid）
     * @return 检查项（含明细）列表
     */
    @PostMapping("/listCiReport")
    public List<CiReportDto> listCiReport(@RequestBody Cireport cireport) {
        return cireportService.listCiReport(cireport);
    }
}
