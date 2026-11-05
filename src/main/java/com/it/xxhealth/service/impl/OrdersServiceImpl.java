package com.it.xxhealth.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.xxhealth.entity.Orders;
import com.it.xxhealth.service.OrdersService;
import com.it.xxhealth.mapper.OrdersMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.it.xxhealth.dto.OrdersPageRequestDto;
import com.it.xxhealth.dto.OrdersPageResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* @author Ljz
* @description 针对表【orders(体检预约订单表)】的数据库操作Service实现
* @createDate 2026-11-04 09:47:06
*/
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders>
    implements OrdersService{

    @Override
    public OrdersPageResponseDto listOrders(OrdersPageRequestDto request) {
        request.normalize();
        Page<Orders> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<Orders> result = baseMapper.selectOrdersPage(page, request);
        return OrdersPageResponseDto.from(result);
    }

    @Override
    public Orders getOrdersById(Integer orderid) {
        return baseMapper.selectOrdersById(orderid);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateOrdersState(Integer orderid, Integer state) {
        Orders orders = new Orders();
        orders.setOrderid(orderid);
        orders.setState(state);
        return updateById(orders) ? 1 : 0;
    }
}




