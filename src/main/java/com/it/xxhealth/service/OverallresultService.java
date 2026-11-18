package com.it.xxhealth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.it.xxhealth.entity.Overallresult;
import java.util.List;

/**
 * 总检结论业务接口。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
public interface OverallresultService extends IService<Overallresult> {

    /**
     * 根据查询条件获取相关业务数据列表。
     * @param orderid 预约订单编号
     * @return 符合条件的数据集合
     */
    List<Overallresult> listOverallResultByOrderId(Integer orderid);

    /**
     * 校验并保存业务数据。
     * @param overallresult 总检结论数据
     * @return 处理结果数值
     */
    int saveOverallResult(Overallresult overallresult);

    /**
     * 校验输入并更新业务记录。
     * @param overallresult 总检结论数据
     * @return 处理结果数值
     */
    int updateOverallResult(Overallresult overallresult);

    /**
     * 按业务规则删除或停用指定记录。
     * @param orid orid 参数
     * @return 处理结果数值
     */
    int removeOverallResult(Integer orid);
 /**
  * 根据查询条件获取相关业务数据列表。
  * @param id 业务主键
  * @return 符合条件的数据集合
  */
 default List<Overallresult> listByOrderId(Integer id) { return listOverallResultByOrderId(id); }
}
