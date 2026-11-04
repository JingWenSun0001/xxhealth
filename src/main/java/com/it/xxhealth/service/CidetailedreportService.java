package com.it.xxhealth.service;

import com.it.xxhealth.entity.Cidetailedreport;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Ljz
* @description 针对表【cidetailedreport(体检报告检查项明细表)】的数据库操作Service
* @createDate 2026-11-04 09:47:06
*/
public interface CidetailedreportService extends IService<Cidetailedreport> {

    /**
     * 更新检查项信息及检查项明细信息。
     * <p>
     * 医生在体检报告编辑页录入检查结果后，逐条更新检查项明细的
     * value（检查结果）与 iserror（是否异常）。
     * </p>
     *
     * @param cidetailedreports 检查项明细列表
     * @return 实际更新的记录数
     */
    int updateCiDetailedReport(List<Cidetailedreport> cidetailedreports);

}
