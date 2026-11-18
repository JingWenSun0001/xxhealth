package com.it.xxhealth.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.xxhealth.entity.Cidetailedreport;
import com.it.xxhealth.service.CidetailedreportService;
import com.it.xxhealth.mapper.CidetailedreportMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 报告检查结果明细业务实现。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
@Service
public class CidetailedreportServiceImpl extends ServiceImpl<CidetailedreportMapper, Cidetailedreport>
    implements CidetailedreportService{

    /**
     * 在同一事务中批量更新检查结果和异常标记。
     * @param cidetailedreports 待更新的报告检查结果集合
     * @return 处理结果数值
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
