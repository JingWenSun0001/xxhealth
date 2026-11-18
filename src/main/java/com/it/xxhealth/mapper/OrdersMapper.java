package com.it.xxhealth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.it.xxhealth.dto.OrdersPageRequestDto;
import com.it.xxhealth.dto.OrdersDto;
import com.it.xxhealth.entity.Orders;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 预约订单数据访问接口。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
public interface OrdersMapper extends BaseMapper<Orders> {

    /**
     * 统计指定用户尚未归档的预约数量。
     * @param userid 用户手机号
     * @return 处理结果数值
     */
    @Select("SELECT COUNT(*) FROM orders WHERE userId = #{userid} AND state = 1")
    int countUnarchived(@Param("userid") String userid);

    /**
     * 按条件查询用户预约列表或分页结果。
     * @param userid 用户手机号
     * @param state 目标业务状态
     * @return 符合条件的数据集合
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
     * 查询包含机构和套餐信息的预约详情。
     * @param orderid 预约订单编号
     * @return 查询到的业务数据；不存在时按接口约定返回 null 或空集合
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
     * @param hpid 体检机构编号
     * @param year 目标年份
     * @param month 目标月份
     * @return 符合条件的数据集合
     */
    @Select("SELECT DAY(orderDate) AS day, COUNT(*) AS cnt FROM orders " +
            "WHERE hpId = #{hpid} AND YEAR(orderDate) = #{year} AND MONTH(orderDate) = #{month} " +
            "GROUP BY DAY(orderDate)")
    List<Map<String, Object>> countByMonth(@Param("hpid") Integer hpid,
                                           @Param("year") int year,
                                           @Param("month") int month);
 /**
  * 根据查询条件获取指定业务数据。
  * @param page MyBatis-Plus 分页对象
  * @param request 分页查询参数
  * @return 符合条件的数据集合
  */
 IPage<Orders> selectOrdersPage(Page<Orders> page, @Param("request") OrdersPageRequestDto request);
 /**
  * 根据查询条件获取指定业务数据。
  * @param orderid 预约订单编号
  * @return 查询到的业务数据；不存在时按接口约定返回 null 或空集合
  */
 Orders selectOrdersById(@Param("orderid") Integer orderid);
}
