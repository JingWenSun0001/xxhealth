package com.it.xxhealth.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.xxhealth.entity.Cidetailedreport;
import com.it.xxhealth.service.CidetailedreportService;
import com.it.xxhealth.mapper.CidetailedreportMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* @author Ljz
* @description 针对表【cidetailedreport(体检报告检查项明细表)】的数据库操作Service实现
* @createDate 2026-11-04 09:47:06
*/
@Service
public class CidetailedreportServiceImpl extends ServiceImpl<CidetailedreportMapper, Cidetailedreport>
    implements CidetailedreportService{

    /**
     * 批量更新检查项明细的检查结果与异常标记。
     * <p>
     * 只更新 value 与 iserror 两个可编辑字段，其余模板字段（名称、单位、参考范围等）保持不变；
     * MyBatis-Plus 的 updateById 默认忽略 null 字段，因此前端未传的字段不会被覆盖。
     * </p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateCiDetailedReport(List<Cidetailedreport> cidetailedreports) {
        if (cidetailedreports == null || cidetailedreports.isEmpty()) {
            return 0;
        }
        int updatedRows = 0;
        for (Cidetailedreport item : cidetailedreports) {
            if (item == null || item.getCidrid() == null) {
                continue;
            }
            Cidetailedreport update = new Cidetailedreport();
            update.setCidrid(item.getCidrid());
            update.setValue(item.getValue());
            update.setIserror(item.getIserror());
            updatedRows += this.baseMapper.updateById(update);
        }
        return updatedRows;
    }
}
