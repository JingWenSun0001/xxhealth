package com.it.xxhealth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.it.xxhealth.dto.OrdersDto;
import com.it.xxhealth.entity.Orders;
import java.util.List;
import com.it.xxhealth.dto.OrdersPageRequestDto;
import com.it.xxhealth.dto.OrdersPageResponseDto;

/**
 * 预约订单业务接口。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
public interface OrdersService extends IService<Orders> {

    /**
     * 按条件查询用户预约列表或分页结果。
     * @param request 分页查询参数
     * @return 查询到的业务数据；不存在时按接口约定返回 null 或空集合
     */
    OrdersPageResponseDto listOrders(OrdersPageRequestDto request);

    /**
     * 根据查询条件获取指定业务数据。
     * @param orderid 预约订单编号
     * @return 查询到的业务数据；不存在时按接口约定返回 null 或空集合
     */
    Orders getOrdersById(Integer orderid);

    /**
     * 更新用户预约归档状态并同步医生端。
     * @param orderid 预约订单编号
     * @param state 目标业务状态
     * @return 处理结果数值
     */
    int updateOrdersState(Integer orderid, Integer state);
 /**
  * 统计指定用户尚未归档的预约数量。
  * @param userid 用户手机号
  * @return 处理结果数值
  */
 int countUnarchived(String userid);
 /**
  * 校验重复预约和预约日期后创建订单并同步医生端。
  * @param orders 预约订单数据
  * @return 处理结果数值
  */
 int saveOrder(Orders orders);
 /**
  * 按条件查询用户预约列表或分页结果。
  * @param userid 用户手机号
  * @param state 目标业务状态
  * @return 符合条件的数据集合
  */
 List<OrdersDto> listOrders(String userid, Integer state);
 /**
  * 查询包含机构和套餐信息的预约详情。
  * @param orderid 预约订单编号
  * @return 查询到的业务数据；不存在时按接口约定返回 null 或空集合
  */
 OrdersDto getOrderDetail(Integer orderid);
 /**
  * 按业务规则取消预约并同步医生端状态。
  * @param orderid 预约订单编号
  * @return 处理结果数值
  */
 int removeOrder(Integer orderid);
}
