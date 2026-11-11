package com.it.xxhealth.service;

import com.it.xxhealth.dto.SetmealDto;
import com.it.xxhealth.entity.Setmeal;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
* @author Ljz
* @description 针对表【setmeal(体检套餐信息表)】的数据库操作Service
* @createDate 2026-11-04 09:47:06
*/
public interface SetmealService extends IService<Setmeal> {

    /**
     * 查询全部套餐（订单模块使用）
     */
    List<Setmeal> listSetmeal();

    /**
     * 按类型查套餐（type: 1 男士 / 0 女士 / null 全部）
     */
    List<Setmeal> listSetmealByType(Integer type);

    /**
     * 套餐详情（套餐 + 检查项列表）
     */
    SetmealDto getSetmealById(Integer smid);
}
