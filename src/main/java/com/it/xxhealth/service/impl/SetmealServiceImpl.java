package com.it.xxhealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.xxhealth.dto.CheckitemDto;
import com.it.xxhealth.dto.SetmealDto;
import com.it.xxhealth.entity.Checkitem;
import com.it.xxhealth.entity.Setmeal;
import com.it.xxhealth.entity.Setmealdetailed;
import com.it.xxhealth.mapper.CheckitemMapper;
import com.it.xxhealth.mapper.SetmealMapper;
import com.it.xxhealth.mapper.SetmealdetailedMapper;
import com.it.xxhealth.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
* @author Ljz
* @description 针对表【setmeal(体检套餐信息表)】的数据库操作Service实现
* @createDate 2026-11-04 09:47:06
*/
@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal>
    implements SetmealService{

    @Resource
    private SetmealdetailedMapper setmealdetailedMapper;

    @Resource
    private CheckitemMapper checkitemMapper;

    @Override
    public List<Setmeal> listSetmeal() {
        return list(new LambdaQueryWrapper<Setmeal>()
                .orderByAsc(Setmeal::getType)
                .orderByAsc(Setmeal::getSmid));
    }

    @Override
    public List<Setmeal> listSetmealByType(Integer type) {
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        if (type != null) {
            wrapper.eq(Setmeal::getType, type);
        }
        wrapper.orderByAsc(Setmeal::getSmid);
        return list(wrapper);
    }

    @Override
    public SetmealDto getSetmealById(Integer smid) {
        if (smid == null) {
            return null;
        }
        Setmeal setmeal = getById(smid);
        if (setmeal == null) {
            return null;
        }

        SetmealDto dto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, dto);

        List<Setmealdetailed> details = setmealdetailedMapper.selectList(
                new LambdaQueryWrapper<Setmealdetailed>()
                        .eq(Setmealdetailed::getSmid, smid)
                        .orderByAsc(Setmealdetailed::getSdid));

        List<CheckitemDto> items = new ArrayList<>();
        for (Setmealdetailed detail : details) {
            Checkitem checkitem = checkitemMapper.selectById(detail.getCiid());
            if (checkitem == null) {
                continue;
            }
            CheckitemDto item = new CheckitemDto();
            item.setCiid(checkitem.getCiid());
            item.setCiname(checkitem.getCiname());
            item.setCicontent(checkitem.getCicontent());
            item.setMeaning(checkitem.getMeaning());
            items.add(item);
        }
        dto.setItems(items);
        return dto;
    }
}
