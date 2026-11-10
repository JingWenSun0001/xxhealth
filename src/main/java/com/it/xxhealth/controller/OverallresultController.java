package com.it.xxhealth.controller;

import com.it.xxhealth.common.Result;
import com.it.xxhealth.entity.Overallresult;
import com.it.xxhealth.service.OverallresultService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/overallResult")
public class OverallResultController {
    private final OverallresultService overallresultService;

    public OverallResultController(OverallresultService overallresultService) {
        this.overallresultService = overallresultService;
    }

    @PostMapping("/listOverallResultByOrderId")
    public Result<List<Overallresult>> listOverallResultByOrderId(@RequestBody Overallresult overallresult) {
        if (overallresult == null || overallresult.getOrderid() == null) {
            return Result.error(400, "预约编号不能为空");
        }
        return Result.success(overallresultService.listOverallResultByOrderId(overallresult.getOrderid()));
    }

    @PostMapping("/saveOverallResult")
    public Result<Integer> saveOverallResult(@RequestBody Overallresult overallresult) {
        String validation = validateForSave(overallresult);
        if (validation != null) {
            return Result.error(400, validation);
        }
        return Result.success(overallresultService.saveOverallResult(overallresult));
    }

    @PostMapping("/updateOverallResult")
    public Result<Integer> updateOverallResult(@RequestBody Overallresult overallresult) {
        if (overallresult == null || overallresult.getOrid() == null) {
            return Result.error(400, "总检结论编号不能为空");
        }
        if (overallresult.getTitle() != null && overallresult.getTitle().trim().isEmpty()) {
            return Result.error(400, "总检结论标题不能为空");
        }
        int updated = overallresultService.updateOverallResult(overallresult);
        return updated == 1 ? Result.success(updated) : Result.error(404, "总检结论不存在");
    }

    @PostMapping("/removeOverallResult")
    public Result<Integer> removeOverallResult(@RequestBody Overallresult overallresult) {
        if (overallresult == null || overallresult.getOrid() == null) {
            return Result.error(400, "总检结论编号不能为空");
        }
        int removed = overallresultService.removeOverallResult(overallresult.getOrid());
        return removed == 1 ? Result.success(removed) : Result.error(404, "总检结论不存在");
    }

    private String validateForSave(Overallresult overallresult) {
        if (overallresult == null || overallresult.getOrderid() == null) {
            return "预约编号不能为空";
        }
        if (overallresult.getTitle() == null || overallresult.getTitle().trim().isEmpty()) {
            return "总检结论标题不能为空";
        }
        return null;
    }
}
