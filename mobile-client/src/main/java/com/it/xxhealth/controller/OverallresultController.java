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

    @PostMapping("/listOverallResultByOrderId")
    public List<Overallresult> listOverallResultByOrderId(@RequestBody Overallresult overallresult) {
        if (overallresult == null || overallresult.getOrderid() == null) {
            return new ArrayList<>();
        }
        return overallresultService.listByOrderId(overallresult.getOrderid());
    }
}
