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
 * 总检结论 Service 实现
 */
@Service
public class OverallresultServiceImpl extends ServiceImpl<OverallresultMapper, Overallresult>
        implements OverallresultService {

    @Override
    public List<Overallresult> listOverallResultByOrderId(Integer orderid) {
        return list(Wrappers.<Overallresult>lambdaQuery()
                .eq(Overallresult::getOrderid, orderid)
                .orderByAsc(Overallresult::getOrid));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveOverallResult(Overallresult overallresult) {
        return save(overallresult) ? 1 : 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateOverallResult(Overallresult overallresult) {
        return updateById(overallresult) ? 1 : 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int removeOverallResult(Integer orid) {
        return removeById(orid) ? 1 : 0;
    }
}
