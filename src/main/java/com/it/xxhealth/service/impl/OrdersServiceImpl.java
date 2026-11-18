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
 * 预约订单业务实现，负责用户预约与医生端数据同步。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    /**
     * 负责把用户和订单同步到医生工作台的服务。
     */
    private final MobileDoctorSyncService mobileDoctorSyncService;

    /**
     * 创建 OrdersServiceImpl 实例并注入所需依赖。
     * @param mobileDoctorSyncService mobileDoctorSyncService 参数
     */
    public OrdersServiceImpl(MobileDoctorSyncService mobileDoctorSyncService) {
        this.mobileDoctorSyncService = mobileDoctorSyncService;
    }

    /**
     * 统计指定用户尚未归档的预约数量。
     * @param userid 用户手机号
     * @return 处理结果数值
     */
    @Override
    public int countUnarchived(String userid) {
        return baseMapper.countUnarchived(userid);
    }

    /**
     * 校验重复预约和预约日期后创建订单并同步医生端。
     * @param orders 预约订单数据
     * @return 处理结果数值
     */
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

    /**
     * 按条件查询用户预约列表或分页结果。
     * @param userid 用户手机号
     * @param state 目标业务状态
     * @return 符合条件的数据集合
     */
    @Override
    public List<OrdersDto> listOrders(String userid, Integer state) {
        return baseMapper.listOrders(userid, state);
    }

    /**
     * 查询包含机构和套餐信息的预约详情。
     * @param orderid 预约订单编号
     * @return 查询到的业务数据；不存在时按接口约定返回 null 或空集合
     */
    @Override
    public OrdersDto getOrderDetail(Integer orderid) {
        return baseMapper.getOrderDetail(orderid);
    }

    /**
     * 按业务规则取消预约并同步医生端状态。
     * @param orderid 预约订单编号
     * @return 处理结果数值
     */
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
 /**
  * 按条件查询用户预约列表或分页结果。
  * @param request 分页查询参数
  * @return 查询到的业务数据；不存在时按接口约定返回 null 或空集合
  */
 public OrdersPageResponseDto listOrders(OrdersPageRequestDto request) { request.normalize(); IPage<Orders> page=baseMapper.selectOrdersPage(new Page<Orders>(request.getPageNum(),request.getPageSize()),request); return OrdersPageResponseDto.from(page); }
 /**
  * 根据查询条件获取指定业务数据。
  * @param id 业务主键
  * @return 查询到的业务数据；不存在时按接口约定返回 null 或空集合
  */
 public Orders getOrdersById(Integer id) { return baseMapper.selectOrdersById(id); }
 /**
  * 更新用户预约归档状态并同步医生端。
  * @param id 业务主键
  * @param state 目标业务状态
  * @return 处理结果数值
  */
 @Transactional public int updateOrdersState(Integer id,Integer state) { Orders order=new Orders(); order.setOrderid(id); order.setState(state); if(!updateById(order))return 0; mobileDoctorSyncService.syncOrder(id); return 1; }
}
