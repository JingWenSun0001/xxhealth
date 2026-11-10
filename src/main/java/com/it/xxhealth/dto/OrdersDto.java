package com.it.xxhealth.dto;

import java.util.Date;
import lombok.Data;

/**
 * 预约订单返回对象（订单 + 医院名 + 套餐名 + 价格）
 */
@Data
public class OrdersDto {

    private Integer orderid;

    private Date orderdate;

    private String userid;

    private Integer hpid;

    private Integer smid;

    private Integer state;

    private String hospitalname;

    private String setmealname;

    private Integer price;
}
