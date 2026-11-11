package com.it.xxhealth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.it.xxhealth.dto.OrdersDto;
import com.it.xxhealth.entity.Orders;
import java.util.List;
import com.it.xxhealth.dto.OrdersPageRequestDto;
import com.it.xxhealth.dto.OrdersPageResponseDto;

/**
 * 体检预约订单 Service
 */
public interface OrdersService extends IService<Orders> {

    OrdersPageResponseDto listOrders(OrdersPageRequestDto request);

    Orders getOrdersById(Integer orderid);

    int updateOrdersState(Integer orderid, Integer state);
 int countUnarchived(String userid);
 int saveOrder(Orders orders);
 List<OrdersDto> listOrders(String userid, Integer state);
 OrdersDto getOrderDetail(Integer orderid);
 int removeOrder(Integer orderid);
}
