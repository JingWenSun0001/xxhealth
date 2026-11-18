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
 * 报告检查项目业务接口。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
public interface CireportService extends IService<Cireport> {

    /**
     * 根据订单套餐复制检查项和明细，生成报告录入模板。
     * @param orders 预约订单数据
     * @return 处理结果数值
     */
    int createReportTemplate(Orders orders);

    /**
     * 查询指定订单的报告检查项目及结果明细。
     * @param cireport 报告检查项目查询条件
     * @return 符合条件的数据集合
     */
    List<CiReportDto> listCiReport(Cireport cireport);

 /**
  * 根据查询条件获取相关业务数据列表。
  * @param id 业务主键
  * @return 符合条件的数据集合
  */
 default List<CiReportDto> listByOrderId(Integer id) { Cireport r=new Cireport(); r.setOrderid(id); return listCiReport(r); }
}
