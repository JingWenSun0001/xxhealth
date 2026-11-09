package com.it.xxhealth.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.xxhealth.entity.Overallresult;
import com.it.xxhealth.mapper.OverallresultMapper;
import com.it.xxhealth.service.OverallresultService;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 总检结论 Service 实现
 */
@Service
public class OverallresultServiceImpl extends ServiceImpl<OverallresultMapper, Overallresult>
        implements OverallresultService {

    @Override
    public List<Overallresult> listByOrderId(Integer orderid) {
        return this.list(Wrappers.<Overallresult>lambdaQuery()
                .eq(Overallresult::getOrderid, orderid)
                .orderByAsc(Overallresult::getOrid));
    }
}
