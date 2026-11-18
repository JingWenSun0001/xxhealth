package com.it.xxhealth.dto;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.it.xxhealth.entity.Orders;
import lombok.Data;

import java.util.List;

/**
 * 医生端预约订单分页查询结果。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
@Data
public class OrdersPageResponseDto {
    /**
     * 当前页码，从 1 开始。
     */
    private long pageNum;
    /**
     * 每页记录数。
     */
    private long pageSize;
    /**
     * 符合条件的记录总数或单日总容量。
     */
    private long total;
    /**
     * pages 业务字段。
     */
    private long pages;
    /**
     * list 业务字段。
     */
    private List<Orders> list;

    /**
     * 根据 MyBatis-Plus 分页结果构造接口分页响应。
     * @param page MyBatis-Plus 分页对象
     * @return 方法处理结果
     */
    public static OrdersPageResponseDto from(IPage<Orders> page) {
        OrdersPageResponseDto response = new OrdersPageResponseDto();
        response.setPageNum(page.getCurrent());
        response.setPageSize(page.getSize());
        response.setTotal(page.getTotal());
        response.setPages(page.getPages());
        response.setList(page.getRecords());
        return response;
    }
}
