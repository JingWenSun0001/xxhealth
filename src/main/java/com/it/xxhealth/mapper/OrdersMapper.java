package com.it.xxhealth.mapper;

import com.it.xxhealth.entity.Orders;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.it.xxhealth.dto.OrdersPageRequestDto;
import org.apache.ibatis.annotations.Param;

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

}




