package com.it.xxhealth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.it.xxhealth.dto.OrdersDto;
import com.it.xxhealth.entity.Orders;
import java.util.List;

/**
 * 体检预约订单 Service
 */
public interface OrdersService extends IService<Orders> {

    /** 未归档订单数量（>0 表示不能再预约） */
    int countUnarchived(String userid);

    /** 创建预约：0 重复预约；-2 日期早于今天；>0 成功 */
    int saveOrder(Orders orders);

    /** 我的预约列表，state 为 null 查全部 */
    List<OrdersDto> listOrders(String userid, Integer state);

    /** 订单详情 */
    OrdersDto getOrderDetail(Integer orderid);

    /** 取消预约：-1 已归档不能取消；0 不存在；>0 成功 */
    int removeOrder(Integer orderid);
}
