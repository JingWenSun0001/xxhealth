package com.it.xxhealth.service;

import com.it.xxhealth.entity.Cidetailedreport;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 报告检查结果明细业务接口。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
public interface CidetailedreportService extends IService<Cidetailedreport> {

    /**
     * 在同一事务中批量更新检查结果和异常标记。
     * @param cidetailedreports 待更新的报告检查结果集合
     * @return 处理结果数值
     */
    int updateCiDetailedReport(List<Cidetailedreport> cidetailedreports);

}
