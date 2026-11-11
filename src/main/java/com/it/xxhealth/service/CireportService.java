package com.it.xxhealth.service;

import com.it.xxhealth.dto.CiReportDto;
import com.it.xxhealth.entity.Cireport;
import com.it.xxhealth.entity.Orders;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Ljz
* @description 针对表【cireport(体检报告检查项信息表)】的数据库操作Service
* @createDate 2026-11-04 09:47:06
*/
public interface CireportService extends IService<Cireport> {

    /**
     * 根据预约订单信息创建体检报告模板
     *
     * @param orders 预约订单对象（至少包含 orderid）
     * @return 本次创建的检查项数量；订单不存在、套餐无检查项或模板已存在时返回 0
     */
    int createReportTemplate(Orders orders);

    /**
     * 根据体检预约编号查询检查项列表及明细信息
     *
     * @param cireport 检查项报告对象（至少包含 orderid）
     * @return 检查项（含明细）列表，查不到时返回空列表
     */
    List<CiReportDto> listCiReport(Cireport cireport);

<<<<<<< HEAD
=======
 default List<CiReportDto> listByOrderId(Integer id) { Cireport r=new Cireport(); r.setOrderid(id); return listCiReport(r); }
>>>>>>> upstream/master
}
