package com.it.xxhealth.service;

import com.it.xxhealth.entity.Overallresult;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
* @author Ljz
* @description 针对表【overallresult(总检结论信息表)】的数据库操作Service
* @createDate 2026-11-04 09:47:06
*/
public interface OverallresultService extends IService<Overallresult> {

    List<Overallresult> listOverallResultByOrderId(Integer orderid);

    int saveOverallResult(Overallresult overallresult);

    int updateOverallResult(Overallresult overallresult);

    int removeOverallResult(Integer orid);
}
