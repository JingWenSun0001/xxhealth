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
 * 用户端预约订单接口，负责编排预约查询、创建、取消和归档操作。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
@RestController
@RequestMapping("/orders")
public class OrdersController {

    /**
     * 预约订单业务服务。
     */
    @Autowired
    private OrdersService ordersService;

    /**
     * 按条件查询用户预约列表或分页结果。
     * @param request 分页查询参数
     * @return 查询到的业务数据；不存在时按接口约定返回 null 或空集合
     */
    @PostMapping("/listOrders")
    public com.it.xxhealth.common.Result<com.it.xxhealth.dto.OrdersPageResponseDto> listOrders(
            @RequestBody(required=false) com.it.xxhealth.dto.OrdersPageRequestDto request) {
        return com.it.xxhealth.common.Result.success(ordersService.listOrders(request==null?new com.it.xxhealth.dto.OrdersPageRequestDto():request));
    }

    /**
     * 更新用户预约归档状态并同步医生端。
     * @param orders 预约订单数据
     * @return 保存、更新或同步后的业务结果
     */
    @PostMapping("/updateOrdersState")
    public com.it.xxhealth.common.Result<Integer> updateOrdersState(@RequestBody Orders orders) {
        if(orders.getOrderid()==null||orders.getState()==null||(orders.getState()!=1&&orders.getState()!=2))
            return com.it.xxhealth.common.Result.error(400,"请提供有效的预约编号和归档状态");
        return com.it.xxhealth.common.Result.success(ordersService.updateOrdersState(orders.getOrderid(),orders.getState()));
    }

    /**
     * 根据手机号查询是否有未归档预约，返回未归档订单数量
     * @param orders 预约订单数据
     * @return 处理结果数值
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
     * @param orders 预约订单数据
     * @return 处理结果数值
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
     * @param orders 预约订单数据
     * @return 符合条件的数据集合
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
     * @param orders 预约订单数据
     * @return 处理结果数值
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
     * @param orders 预约订单数据
     * @return 查询到的业务数据；不存在时按接口约定返回 null 或空集合
     */
    @PostMapping("/getOrdersById")
    public OrdersDto getOrdersById(@RequestBody Orders orders) {
        if (orders == null || orders.getOrderid() == null) {
            return null;
        }
        return ordersService.getOrderDetail(orders.getOrderid());
    }
}
