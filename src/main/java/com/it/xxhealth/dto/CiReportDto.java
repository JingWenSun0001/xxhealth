package com.it.xxhealth.dto;

import com.it.xxhealth.entity.Cidetailedreport;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 报告检查项及检查结果明细的数据传输对象。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
@Data
public class CiReportDto implements Serializable {
    /**
     * 序列化版本号。
     */
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
 /**
  * 根据查询条件获取指定业务数据。
  * @return 符合条件的数据集合
  */
 public List<Cidetailedreport> getDetails() { return cidetailedreports; }
}
