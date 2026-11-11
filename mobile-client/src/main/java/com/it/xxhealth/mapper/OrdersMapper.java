package com.it.xxhealth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.it.xxhealth.dto.OrdersDto;
import com.it.xxhealth.entity.Orders;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 体检预约订单 Mapper
 */
public interface OrdersMapper extends BaseMapper<Orders> {

    /**
     * 统计某用户未归档（state=1）的订单数量
     */
    @Select("SELECT COUNT(*) FROM orders WHERE userId = #{userid} AND state = 1")
    int countUnarchived(@Param("userid") String userid);

    /**
     * 按用户查询订单列表（联表带出医院名、套餐名、价格）
     */
    @Select("<script>" +
            "SELECT o.orderId AS orderid, o.orderDate AS orderdate, o.userId AS userid, " +
            "o.hpId AS hpid, o.smId AS smid, o.state, " +
            "h.name AS hospitalname, s.name AS setmealname, s.price AS price " +
            "FROM orders o " +
            "LEFT JOIN hospital h ON o.hpId = h.hpId " +
            "LEFT JOIN setmeal s ON o.smId = s.smId " +
            "WHERE o.userId = #{userid} " +
            "<if test='state != null'>AND o.state = #{state} </if>" +
            "ORDER BY o.orderDate DESC, o.orderId DESC" +
            "</script>")
    List<OrdersDto> listOrders(@Param("userid") String userid, @Param("state") Integer state);

    /**
     * 按订单号查询订单详情
     */
    @Select("SELECT o.orderId AS orderid, o.orderDate AS orderdate, o.userId AS userid, " +
            "o.hpId AS hpid, o.smId AS smid, o.state, " +
            "h.name AS hospitalname, s.name AS setmealname, s.price AS price " +
            "FROM orders o " +
            "LEFT JOIN hospital h ON o.hpId = h.hpId " +
            "LEFT JOIN setmeal s ON o.smId = s.smId " +
            "WHERE o.orderId = #{orderid}")
    OrdersDto getOrderDetail(@Param("orderid") Integer orderid);

    /**
     * 统计某医院某月每天的预约数量
     */
    @Select("SELECT DAY(orderDate) AS day, COUNT(*) AS cnt FROM orders " +
            "WHERE hpId = #{hpid} AND YEAR(orderDate) = #{year} AND MONTH(orderDate) = #{month} " +
            "GROUP BY DAY(orderDate)")
    List<Map<String, Object>> countByMonth(@Param("hpid") Integer hpid,
                                           @Param("year") int year,
                                           @Param("month") int month);
}
