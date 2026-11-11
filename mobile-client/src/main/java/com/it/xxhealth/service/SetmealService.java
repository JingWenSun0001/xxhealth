package com.it.xxhealth.service;

import com.it.xxhealth.dto.SetmealDto;
import com.it.xxhealth.entity.Setmeal;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * 体检套餐信息表 Service
 */
public interface SetmealService extends IService<Setmeal> {

    /**
     * 按类型查套餐
     *
     * @param type 套餐类型：1 男士 / 0 女士；传 null 查全部
     * @return 套餐列表，无数据时返回空集合
     */
    List<Setmeal> listSetmealByType(Integer type);

    /**
     * 套餐详情（含套餐内的检查项）
     *
     * @param smid 套餐编号
     * @return 套餐详情，套餐不存在时返回 null
     */
    SetmealDto getSetmealById(Integer smid);
}
