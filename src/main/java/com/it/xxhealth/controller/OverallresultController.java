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
 * 体检报告总检结论接口。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
@RestController
@RequestMapping("/overallResult")
public class OverallresultController {

    /**
     * 总检结论业务服务。
     */
    @Autowired
    private OverallresultService overallresultService;

    /**
     * 校验并保存业务数据。
     * @param body 请求体键值数据
     * @return 保存、更新或同步后的业务结果
     */
    @PostMapping("/saveOverallResult")
    public com.it.xxhealth.common.Result<Integer> saveOverallResult(@RequestBody Overallresult body) {
        if(body.getOrderid()==null||body.getTitle()==null||body.getTitle().trim().isEmpty()) return com.it.xxhealth.common.Result.error(400,"预约编号和结论标题不能为空");
        return com.it.xxhealth.common.Result.success(overallresultService.saveOverallResult(body));
    }
    /**
     * 校验输入并更新业务记录。
     * @param body 请求体键值数据
     * @return 保存、更新或同步后的业务结果
     */
    @PostMapping("/updateOverallResult")
    public com.it.xxhealth.common.Result<Integer> updateOverallResult(@RequestBody Overallresult body) {
        if(body.getOrid()==null) return com.it.xxhealth.common.Result.error(400,"结论编号不能为空");
        return com.it.xxhealth.common.Result.success(overallresultService.updateOverallResult(body));
    }
    /**
     * 按业务规则删除或停用指定记录。
     * @param body 请求体键值数据
     * @return 保存、更新或同步后的业务结果
     */
    @PostMapping("/removeOverallResult")
    public com.it.xxhealth.common.Result<Integer> removeOverallResult(@RequestBody Overallresult body) {
        if(body.getOrid()==null) return com.it.xxhealth.common.Result.error(400,"结论编号不能为空");
        return com.it.xxhealth.common.Result.success(overallresultService.removeOverallResult(body.getOrid()));
    }

    /**
     * 根据查询条件获取相关业务数据列表。
     * @param overallresult 总检结论数据
     * @return 符合条件的数据集合
     */
    @PostMapping("/listOverallResultByOrderId")
    public List<Overallresult> listOverallResultByOrderId(@RequestBody Overallresult overallresult) {
        if (overallresult == null || overallresult.getOrderid() == null) {
            return new ArrayList<>();
        }
        return overallresultService.listByOrderId(overallresult.getOrderid());
    }
}
