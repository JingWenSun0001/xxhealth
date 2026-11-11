package com.it.xxhealth.controller;

import com.it.xxhealth.entity.Overallresult;
import com.it.xxhealth.service.OverallresultService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端 - 总检结论接口（按文档命名）
 */
@RestController
@RequestMapping("/overallResult")
public class OverallresultController {

    @Autowired
    private OverallresultService overallresultService;

    @PostMapping("/saveOverallResult")
    public com.it.xxhealth.common.Result<Integer> saveOverallResult(@RequestBody Overallresult body) {
        if(body.getOrderid()==null||body.getTitle()==null||body.getTitle().trim().isEmpty()) return com.it.xxhealth.common.Result.error(400,"预约编号和结论标题不能为空");
        return com.it.xxhealth.common.Result.success(overallresultService.saveOverallResult(body));
    }
    @PostMapping("/updateOverallResult")
    public com.it.xxhealth.common.Result<Integer> updateOverallResult(@RequestBody Overallresult body) {
        if(body.getOrid()==null) return com.it.xxhealth.common.Result.error(400,"结论编号不能为空");
        return com.it.xxhealth.common.Result.success(overallresultService.updateOverallResult(body));
    }
    @PostMapping("/removeOverallResult")
    public com.it.xxhealth.common.Result<Integer> removeOverallResult(@RequestBody Overallresult body) {
        if(body.getOrid()==null) return com.it.xxhealth.common.Result.error(400,"结论编号不能为空");
        return com.it.xxhealth.common.Result.success(overallresultService.removeOverallResult(body.getOrid()));
    }

    @PostMapping("/listOverallResultByOrderId")
    public List<Overallresult> listOverallResultByOrderId(@RequestBody Overallresult overallresult) {
        if (overallresult == null || overallresult.getOrderid() == null) {
            return new ArrayList<>();
        }
        return overallresultService.listByOrderId(overallresult.getOrderid());
    }
}
