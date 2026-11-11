package com.it.xxhealth.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * 套餐详情返回对象（套餐 + 包含的检查项）
 */
@Data
public class SetmealDto {

    /** 体检套餐编号 */
    private Integer smid;

    /** 体检套餐名称 */
    private String name;

    /** 体检套餐类型 (1:男士套餐; 0:女士套餐) */
    private Integer type;

    /** 体检套餐价格（元） */
    private Integer price;

    /** 套餐包含的检查项，无检查项时为空数组 */
    private List<CheckitemDto> items = new ArrayList<>();
}
