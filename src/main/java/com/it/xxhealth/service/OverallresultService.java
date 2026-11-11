package com.it.xxhealth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.it.xxhealth.entity.Overallresult;
import java.util.List;

/**
 * 总检结论 Service
 */
public interface OverallresultService extends IService<Overallresult> {

    List<Overallresult> listOverallResultByOrderId(Integer orderid);

    int saveOverallResult(Overallresult overallresult);

    int updateOverallResult(Overallresult overallresult);

    int removeOverallResult(Integer orid);
 default List<Overallresult> listByOrderId(Integer id) { return listOverallResultByOrderId(id); }
}
