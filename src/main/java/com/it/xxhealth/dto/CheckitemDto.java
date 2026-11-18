package com.it.xxhealth.dto;

import lombok.Data;

/**
 * 检查项目及其检查明细的数据传输对象。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
@Data
public class CheckitemDto {

    /**
     * 检查项编号
     */
    private Integer ciid;

    /**
     * 检查项名称
     */
    private String ciname;

    /**
     * 检查项内容
     */
    private String cicontent;

    /**
     * 检查项意义
     */
    private String meaning;
}
