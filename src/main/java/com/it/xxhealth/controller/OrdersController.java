package com.it.xxhealth.controller;

import com.it.xxhealth.common.Result;
import com.it.xxhealth.dto.OrdersPageRequestDto;
import com.it.xxhealth.dto.OrdersPageResponseDto;
import com.it.xxhealth.entity.Orders;
import com.it.xxhealth.service.OrdersService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrdersController {
    private final OrdersService ordersService;

    public OrdersController(OrdersService ordersService) {
        this.ordersService = ordersService;
    }

    @PostMapping("/listOrders")
    public Result<OrdersPageResponseDto> listOrders(
            @RequestBody(required = false) OrdersPageRequestDto request) {
        if (request == null) {
            request = new OrdersPageRequestDto();
        }
        return Result.success(ordersService.listOrders(request));
    }

    @PostMapping("/getOrdersById")
    public Result<Orders> getOrdersById(@RequestBody Orders orders) {
        if (orders == null || orders.getOrderid() == null) {
            return Result.error(400, "预约编号不能为空");
        }
        Orders result = ordersService.getOrdersById(orders.getOrderid());
        return result == null ? Result.error(404, "预约订单不存在") : Result.success(result);
    }

    @PostMapping("/updateOrdersState")
    public Result<Integer> updateOrdersState(@RequestBody Orders orders) {
        if (orders == null || orders.getOrderid() == null) {
            return Result.error(400, "预约编号不能为空");
        }
        if (orders.getState() == null || (orders.getState() != 1 && orders.getState() != 2)) {
            return Result.error(400, "订单状态只能为1（未归档）或2（已归档）");
        }
        int updated = ordersService.updateOrdersState(orders.getOrderid(), orders.getState());
        return updated == 1 ? Result.success(updated) : Result.error(404, "预约订单不存在");
    }
}
