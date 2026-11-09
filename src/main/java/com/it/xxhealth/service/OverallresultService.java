package com.it.xxhealth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.it.xxhealth.entity.Overallresult;
import java.util.List;

/**
 * 总检结论 Service
 */
public interface OverallresultService extends IService<Overallresult> {

    /** 按预约编号查询总检结论 */
    List<Overallresult> listByOrderId(Integer orderid);
}
