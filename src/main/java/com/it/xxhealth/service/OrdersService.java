package com.it.xxhealth.service;

import com.it.xxhealth.entity.Orders;
import com.baomidou.mybatisplus.extension.service.IService;
import com.it.xxhealth.dto.OrdersPageRequestDto;
import com.it.xxhealth.dto.OrdersPageResponseDto;

/**
* @author Ljz
* @description 针对表【orders(体检预约订单表)】的数据库操作Service
* @createDate 2026-11-04 09:47:06
*/
public interface OrdersService extends IService<Orders> {

    OrdersPageResponseDto listOrders(OrdersPageRequestDto request);

    Orders getOrdersById(Integer orderid);

    int updateOrdersState(Integer orderid, Integer state);
}
