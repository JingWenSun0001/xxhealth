package com.it.xxhealth.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.xxhealth.dto.OrdersDto;
import com.it.xxhealth.dto.OrdersPageRequestDto;
import com.it.xxhealth.dto.OrdersPageResponseDto;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.it.xxhealth.entity.Orders;
import com.it.xxhealth.doctor.MobileDoctorSyncService;
import com.it.xxhealth.mapper.OrdersMapper;
import com.it.xxhealth.service.OrdersService;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 体检预约订单 Service 实现
 */
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    private final MobileDoctorSyncService mobileDoctorSyncService;

    public OrdersServiceImpl(MobileDoctorSyncService mobileDoctorSyncService) {
        this.mobileDoctorSyncService = mobileDoctorSyncService;
    }

    @Override
    public int countUnarchived(String userid) {
        return baseMapper.countUnarchived(userid);
    }

    @Override
    @Transactional
    public int saveOrder(Orders orders) {
        // 有未归档预约，不能再约
        if (countUnarchived(orders.getUserid()) > 0) {
            return 0;
        }
        // 预约日期不能早于今天
        Date date = orders.getOrderdate();
        if (date != null) {
            LocalDate target = date instanceof java.sql.Date
                    ? ((java.sql.Date) date).toLocalDate()
                    : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (target.isBefore(LocalDate.now())) {
                return -2;
            }
        }
        orders.setOrderid(null);
        orders.setState(1);
        int rows = baseMapper.insert(orders);
        if (rows > 0) {
            mobileDoctorSyncService.syncOrder(orders.getOrderid());
        }
        return rows;
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
    @Transactional
    public int removeOrder(Integer orderid) {
        Orders db = baseMapper.selectById(orderid);
        if (db == null) {
            return 0;
        }
        if (db.getState() != null && db.getState() == 2) {
            return -1;
        }
        // 保留已取消订单，手机端才能展示医生端同步后的取消历史；state=3 不计入未归档预约。
        Orders cancelled = new Orders();
        cancelled.setOrderid(orderid);
        cancelled.setState(3);
        int rows = baseMapper.updateById(cancelled);
        if (rows > 0) {
            mobileDoctorSyncService.cancelOrder(orderid);
        }
        return rows;
    }
 public OrdersPageResponseDto listOrders(OrdersPageRequestDto request) { request.normalize(); IPage<Orders> page=baseMapper.selectOrdersPage(new Page<Orders>(request.getPageNum(),request.getPageSize()),request); return OrdersPageResponseDto.from(page); }
 public Orders getOrdersById(Integer id) { return baseMapper.selectOrdersById(id); }
 @Transactional public int updateOrdersState(Integer id,Integer state) { Orders order=new Orders(); order.setOrderid(id); order.setState(state); if(!updateById(order))return 0; mobileDoctorSyncService.syncOrder(id); return 1; }
}
