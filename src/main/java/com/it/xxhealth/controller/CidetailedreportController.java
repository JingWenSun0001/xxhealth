package com.it.xxhealth.controller;

import com.it.xxhealth.common.Result;
import com.it.xxhealth.entity.Cidetailedreport;
import com.it.xxhealth.service.CidetailedreportService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 医生端：体检报告检查项明细接口
 */
@RestController
@RequestMapping("/ciDetailedReport")
public class CidetailedreportController {

    @Resource
    private CidetailedreportService cidetailedreportService;

    /**
     * 更新检查项信息及检查项明细信息
     *
     * @param cidetailedreports 检查项明细列表（JSON 数组）
     * @return 实际更新的记录数
     */
    @PostMapping("/updateCiDetailedReport")
    public Result<Integer> updateCiDetailedReport(@RequestBody List<Cidetailedreport> cidetailedreports) {
        return Result.success(cidetailedreportService.updateCiDetailedReport(cidetailedreports));
    }
}
