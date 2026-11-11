package com.it.xxhealth.dto;

import lombok.Data;

/**
 * 套餐详情中的检查项返回对象（只暴露前端需要的字段）
 */
@Data
public class CheckitemDto {

    /** 检查项编号 */
    private Integer ciid;

    /** 检查项名称 */
    private String ciname;

    /** 检查项内容 */
    private String cicontent;

    /** 检查项意义 */
    private String meaning;
}
