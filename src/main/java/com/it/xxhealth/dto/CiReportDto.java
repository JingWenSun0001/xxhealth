package com.it.xxhealth.dto;

import com.it.xxhealth.entity.Cidetailedreport;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * 报告检查项 + 明细
 */
@Data
public class CiReportDto {

    private Integer cirid;

    private Integer ciid;

    private String ciname;

    private Integer orderid;

    /**
     * 该检查项下的明细列表（字段名与前端 doctor 端页面保持一致）
     */
    private List<Cidetailedreport> cidetailedreports = new ArrayList<>();
}
