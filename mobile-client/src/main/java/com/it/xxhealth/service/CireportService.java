package com.it.xxhealth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.it.xxhealth.dto.CiReportDto;
import com.it.xxhealth.entity.Cireport;
import java.util.List;

/**
 * 报告检查项 Service
 */
public interface CireportService extends IService<Cireport> {

    /** 按预约编号查询检查项列表（含明细） */
    List<CiReportDto> listByOrderId(Integer orderid);
}
