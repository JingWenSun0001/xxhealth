package com.it.xxhealth.controller;

import com.it.xxhealth.dto.CiReportDto;
import com.it.xxhealth.entity.Cireport;
import com.it.xxhealth.service.CireportService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端 - 报告检查项接口（按文档命名）
 */
@RestController
@RequestMapping("/ciReport")
public class CireportController {

    @Autowired
    private CireportService cireportService;

    @PostMapping("/listCiReport")
    public List<CiReportDto> listCiReport(@RequestBody Cireport cireport) {
        if (cireport == null || cireport.getOrderid() == null) {
            return new ArrayList<>();
        }
        return cireportService.listByOrderId(cireport.getOrderid());
    }
}
