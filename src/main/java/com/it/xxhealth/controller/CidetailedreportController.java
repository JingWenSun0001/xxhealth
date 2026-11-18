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
 * 体检报告明细接口，负责批量保存检查结果和异常标记。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
@RestController
@RequestMapping("/ciDetailedReport")
public class CidetailedreportController {

    /**
     * 报告检查明细业务服务。
     */
    @Resource
    private CidetailedreportService cidetailedreportService;

    /**
     * 在同一事务中批量更新检查结果和异常标记。
     * @param cidetailedreports 待更新的报告检查结果集合
     * @return 保存、更新或同步后的业务结果
     */
    @PostMapping("/updateCiDetailedReport")
    public Result<Integer> updateCiDetailedReport(@RequestBody List<Cidetailedreport> cidetailedreports) {
        return Result.success(cidetailedreportService.updateCiDetailedReport(cidetailedreports));
    }
}
