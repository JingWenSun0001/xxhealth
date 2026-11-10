package com.it.xxhealth.service;

import com.it.xxhealth.entity.Setmeal;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
* @author Ljz
* @description 针对表【setmeal(体检套餐信息表)】的数据库操作Service
* @createDate 2026-11-04 09:47:06
*/
public interface SetmealService extends IService<Setmeal> {

    List<Setmeal> listSetmeal();
}
