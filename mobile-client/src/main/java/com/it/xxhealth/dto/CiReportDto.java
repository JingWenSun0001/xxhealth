package com.it.xxhealth.dto;

import com.it.xxhealth.entity.Cidetailedreport;
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

    private List<Cidetailedreport> details;
}
