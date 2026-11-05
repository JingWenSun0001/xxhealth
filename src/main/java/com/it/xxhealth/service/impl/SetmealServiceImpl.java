package com.it.xxhealth.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.xxhealth.entity.Setmeal;
import com.it.xxhealth.service.SetmealService;
import com.it.xxhealth.mapper.SetmealMapper;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import java.util.List;

/**
* @author Ljz
* @description 针对表【setmeal(体检套餐信息表)】的数据库操作Service实现
* @createDate 2026-11-04 09:47:06
*/
@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal>
    implements SetmealService{

    @Override
    public List<Setmeal> listSetmeal() {
        return list(Wrappers.<Setmeal>lambdaQuery().orderByAsc(Setmeal::getType, Setmeal::getSmid));
    }
}




