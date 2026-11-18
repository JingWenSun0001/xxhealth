package com.it.xxhealth.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.xxhealth.entity.Overallresult;
import com.it.xxhealth.mapper.OverallresultMapper;
import com.it.xxhealth.service.OverallresultService;
import java.util.List;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 总检结论业务实现。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
@Service
public class OverallresultServiceImpl extends ServiceImpl<OverallresultMapper, Overallresult>
        implements OverallresultService {

    /**
     * 根据查询条件获取相关业务数据列表。
     * @param orderid 预约订单编号
     * @return 符合条件的数据集合
     */
    @Override
    public List<Overallresult> listOverallResultByOrderId(Integer orderid) {
        return list(Wrappers.<Overallresult>lambdaQuery()
                .eq(Overallresult::getOrderid, orderid)
                .orderByAsc(Overallresult::getOrid));
    }

    /**
     * 校验并保存业务数据。
     * @param overallresult 总检结论数据
     * @return 处理结果数值
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveOverallResult(Overallresult overallresult) {
        return save(overallresult) ? 1 : 0;
    }

    /**
     * 校验输入并更新业务记录。
     * @param overallresult 总检结论数据
     * @return 处理结果数值
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateOverallResult(Overallresult overallresult) {
        return updateById(overallresult) ? 1 : 0;
    }

    /**
     * 按业务规则删除或停用指定记录。
     * @param orid orid 参数
     * @return 处理结果数值
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int removeOverallResult(Integer orid) {
        return removeById(orid) ? 1 : 0;
    }
}
