package com.it.xxhealth.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.xxhealth.dto.OrdersDto;
import com.it.xxhealth.entity.Orders;
import com.it.xxhealth.mapper.OrdersMapper;
import com.it.xxhealth.service.OrdersService;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 体检预约订单 Service 实现
 */
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    @Override
    public int countUnarchived(String userid) {
        return baseMapper.countUnarchived(userid);
    }

    @Override
    public int saveOrder(Orders orders) {
        // 有未归档预约，不能再约
        if (countUnarchived(orders.getUserid()) > 0) {
            return 0;
        }
        // 预约日期不能早于今天
        Date date = orders.getOrderdate();
        if (date != null) {
            LocalDate target = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (target.isBefore(LocalDate.now())) {
                return -2;
            }
        }
        orders.setOrderid(null);
        orders.setState(1);
        return baseMapper.insert(orders);
    }

    @Override
    public List<OrdersDto> listOrders(String userid, Integer state) {
        return baseMapper.listOrders(userid, state);
    }

    @Override
    public OrdersDto getOrderDetail(Integer orderid) {
        return baseMapper.getOrderDetail(orderid);
    }

    @Override
    public int removeOrder(Integer orderid) {
        Orders db = baseMapper.selectById(orderid);
        if (db == null) {
            return 0;
        }
        if (db.getState() != null && db.getState() == 2) {
            return -1;
        }
        return baseMapper.deleteById(orderid);
    }
}
