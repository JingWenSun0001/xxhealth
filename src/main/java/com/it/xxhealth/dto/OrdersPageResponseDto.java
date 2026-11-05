package com.it.xxhealth.dto;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.it.xxhealth.entity.Orders;
import lombok.Data;

import java.util.List;

/**
 * 医生端预约订单分页查询结果。
 */
@Data
public class OrdersPageResponseDto {
    private long pageNum;
    private long pageSize;
    private long total;
    private long pages;
    private List<Orders> list;

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
