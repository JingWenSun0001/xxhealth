package com.it.xxhealth.dto;

import com.it.xxhealth.entity.Cidetailedreport;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 体检报告检查项（含检查项明细）返回对象。
 * <p>
 * 与 {@link com.it.xxhealth.entity.Cireport} 相比，额外携带该检查项下挂的明细列表，
 * 用于 {@code ciReport/listCiReport} 接口一次性返回"检查项 + 明细"的树形结构。
 * </p>
 */
@Data
public class CiReportDto implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 检查项报告主键
     */
    private Integer cirid;

    /**
     * 检查项编号
     */
    private Integer ciid;

    /**
     * 检查项名称
     */
    private String ciname;

    /**
     * 所属预约编号
     */
    private Integer orderid;

    /**
     * 该检查项下的明细列表
     */
    private List<Cidetailedreport> cidetailedreports = new ArrayList<>();
}
