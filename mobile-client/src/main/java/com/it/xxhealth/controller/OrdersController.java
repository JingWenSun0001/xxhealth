package com.it.xxhealth.controller;

import com.it.xxhealth.dto.OrdersDto;
import com.it.xxhealth.entity.Orders;
import com.it.xxhealth.service.OrdersService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端 - 预约订单接口（按文档命名）
 */
@RestController
@RequestMapping("/orders")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    /**
     * 根据手机号查询是否有未归档预约，返回未归档订单数量
     */
    @PostMapping("/getOrdersByUserId")
    public int getOrdersByUserId(@RequestBody Orders orders) {
        if (orders == null || orders.getUserid() == null) {
            return 0;
        }
        return ordersService.countUnarchived(orders.getUserid());
    }

    /**
     * 添加体检预约，返回 1 成功；0 重复预约；-2 日期早于今天
     */
    @PostMapping("/saveOrders")
    public int saveOrders(@RequestBody Orders orders) {
        if (orders == null || orders.getUserid() == null || orders.getHpid() == null
                || orders.getSmid() == null || orders.getOrderdate() == null) {
            return 0;
        }
        return ordersService.saveOrder(orders);
    }

    /**
     * 根据用户编号和订单状态查询预约列表，state 为 null 查全部
     */
    @PostMapping("/listOrdersByUserId")
    public List<OrdersDto> listOrdersByUserId(@RequestBody Orders orders) {
        if (orders == null || orders.getUserid() == null) {
            return new ArrayList<>();
        }
        return ordersService.listOrders(orders.getUserid(), orders.getState());
    }

    /**
     * 根据预约编号删除预约（只能删未归档的），返回 1 成功；-1 已归档；0 不存在
     */
    @PostMapping("/removeOrders")
    public int removeOrders(@RequestBody Orders orders) {
        if (orders == null || orders.getOrderid() == null) {
            return 0;
        }
        return ordersService.removeOrder(orders.getOrderid());
    }

    /**
     * 根据预约编号查询预约详情
     */
    @PostMapping("/getOrdersById")
    public OrdersDto getOrdersById(@RequestBody Orders orders) {
        if (orders == null || orders.getOrderid() == null) {
            return null;
        }
        return ordersService.getOrderDetail(orders.getOrderid());
    }
}
