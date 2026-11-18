package com.it.xxhealth.service;

import com.it.xxhealth.dto.SetmealDto;
import com.it.xxhealth.entity.Setmeal;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * 体检套餐业务接口。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
public interface SetmealService extends IService<Setmeal> {

    /**
     * 按适用性别查询体检套餐。
     * @param type type 参数
     * @return 符合条件的数据集合
     */
    List<Setmeal> listSetmealByType(Integer type);

    /**
     * 根据套餐编号查询套餐及检查项目详情。
     * @param smid 体检套餐编号
     * @return 查询到的业务数据；不存在时按接口约定返回 null 或空集合
     */
    SetmealDto getSetmealById(Integer smid);
 /**
  * 查询全部体检套餐及其检查项目。
  * @return 符合条件的数据集合
  */
 List<Setmeal> listSetmeal();
}
