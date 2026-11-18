package com.it.xxhealth.dto;

import java.util.Date;
import lombok.Data;

/**
 * 预约订单详情数据传输对象，包含机构和套餐展示信息。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
@Data
public class OrdersDto {

    /**
     * 预约订单编号。
     */
    private Integer orderid;

    /**
     * 预约体检日期。
     */
    private Date orderdate;

    /**
     * 作为登录账号使用的用户手机号。
     */
    private String userid;

    /**
     * 体检机构编号。
     */
    private Integer hpid;

    /**
     * 体检套餐编号。
     */
    private Integer smid;

    /**
     * 业务状态编码。
     */
    private Integer state;

    /**
     * 体检机构名称。
     */
    private String hospitalname;

    /**
     * 体检套餐名称。
     */
    private String setmealname;

    /**
     * 套餐销售价格。
     */
    private Integer price;
}
