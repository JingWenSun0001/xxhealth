package com.it.xxhealth.service;

import com.it.xxhealth.dto.CiReportDto;
import com.it.xxhealth.entity.Cireport;
import com.it.xxhealth.entity.Orders;
import com.baomidou.mybatisplus.extension.service.IService;
import com.it.xxhealth.dto.CiReportDto;
import com.it.xxhealth.entity.Cireport;
import java.util.List;

import java.util.List;

/**
 * 报告检查项 Service
 */
public interface CireportService extends IService<Cireport> {

    /**
     * 根据预约订单信息创建体检报告模板。
     * <p>
     * 处理流程：预约订单 -&gt; 体检套餐项目明细(setmealdetailed) -&gt; 检查项(checkitem)
     * -&gt; 检查项明细(checkitemdetailed)，最终把模板数据写入
     * cireport（检查项报告）与 cidetailedreport（检查项明细报告）。
     * </p>
     *
     * @param orders 预约订单对象，至少需要包含 orderid
     * @return 本次创建的检查项数量；订单不存在、套餐无检查项或模板已存在时返回 0
     */
    int createReportTemplate(Orders orders);

    /**
     * 根据体检预约编号查询检查项列表及明细信息。
     *
     * @param cireport 检查项报告对象，至少需要包含 orderid
     * @return 检查项（含明细）列表，查不到时返回空列表
     */
    List<CiReportDto> listCiReport(Cireport cireport);

 default List<CiReportDto> listByOrderId(Integer id) { Cireport r=new Cireport(); r.setOrderid(id); return listCiReport(r); }
}
