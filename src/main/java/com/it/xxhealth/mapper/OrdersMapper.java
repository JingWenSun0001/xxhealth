package com.it.xxhealth.mapper;

import com.it.xxhealth.entity.Orders;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.it.xxhealth.dto.OrdersPageRequestDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
* @author Ljz
* @description 针对表【orders(体检预约订单表)】的数据库操作Mapper
* @createDate 2026-11-04 09:47:06
* @Entity generator.domain.Orders
*/
public interface OrdersMapper extends BaseMapper<Orders> {

    IPage<Orders> selectOrdersPage(Page<Orders> page,
                                   @Param("request") OrdersPageRequestDto request);

    Orders selectOrdersById(@Param("orderid") Integer orderid);

    /**
     * 统计某医院某月每天的预约数量（用于预约日历）
     *
     * @return 每行包含 day（几号）、cnt（数量）
     */
    List<Map<String, Object>> countByMonth(@Param("hpid") Integer hpid,
                                           @Param("year") Integer year,
                                           @Param("month") Integer month);

}




