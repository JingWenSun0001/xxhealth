package com.it.xxhealth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 医生端预约订单分页查询参数。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
@Data
public class OrdersPageRequestDto {
    /**
     * 当前页码，从 1 开始。
     */
    private long pageNum = 1;
    /**
     * 每页记录数。
     */
    private long pageSize = 10;
    /**
     * 预约订单编号。
     */
    private Integer orderid;
    /**
     * 作为登录账号使用的用户手机号。
     */
    private String userid;
    /**
     * 用户或医生真实姓名。
     */
    private String realname;
    /**
     * 性别编码，用户端使用 1 表示男、0 表示女。
     */
    private Integer sex;
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
     * 查询开始日期。
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startDate;

    /**
     * 查询结束日期。
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endDate;

    /**
     * 规范化分页参数和文本查询条件。
     */
    public void normalize() {
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        } else if (pageSize > 100) {
            pageSize = 100;
        }
        userid = trimToNull(userid);
        realname = trimToNull(realname);
    }

    /**
     * 去除文本首尾空白，并将空字符串转换为 null。
     * @param value 待转换或校验的输入值
     * @return 方法处理结果
     */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
