package com.it.xxhealth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 医生端预约订单分页查询条件。
 */
@Data
public class OrdersPageRequestDto {
    private long pageNum = 1;
    private long pageSize = 10;
    private Integer orderid;
    private String userid;
    private String realname;
    private Integer sex;
    private Integer hpid;
    private Integer smid;
    private Integer state;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startDate;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endDate;

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

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
