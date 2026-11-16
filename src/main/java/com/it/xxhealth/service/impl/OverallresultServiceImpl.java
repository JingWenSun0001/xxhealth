package com.it.xxhealth.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.xxhealth.entity.Overallresult;
import com.it.xxhealth.service.OverallresultService;
import com.it.xxhealth.mapper.OverallresultMapper;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* @author Ljz
* @description 针对表【overallresult(总检结论信息表)】的数据库操作Service实现
* @createDate 2026-11-04 09:47:06
*/
@Service
public class OverallresultServiceImpl extends ServiceImpl<OverallresultMapper, Overallresult>
    implements OverallresultService{

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




