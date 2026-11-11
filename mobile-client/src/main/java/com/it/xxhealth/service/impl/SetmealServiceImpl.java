package com.it.xxhealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.xxhealth.dto.CheckitemDto;
import com.it.xxhealth.dto.SetmealDto;
import com.it.xxhealth.entity.Checkitem;
import com.it.xxhealth.entity.Setmeal;
import com.it.xxhealth.entity.Setmealdetailed;
import com.it.xxhealth.service.CheckitemService;
import com.it.xxhealth.service.SetmealService;
import com.it.xxhealth.service.SetmealdetailedService;
import com.it.xxhealth.mapper.SetmealMapper;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 体检套餐信息表 Service 实现
 */
@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal>
    implements SetmealService {

    @Autowired
    private SetmealdetailedService setmealdetailedService;

    @Autowired
    private CheckitemService checkitemService;

    /**
     * 按类型查套餐：type 为 null 查全部
     */
    @Override
    public List<Setmeal> listSetmealByType(Integer type) {
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        if (type != null) {
            wrapper.eq(Setmeal::getType, type);
        }
        wrapper.orderByAsc(Setmeal::getSmid);
        return baseMapper.selectList(wrapper);
    }

    /**
     * 套餐详情：套餐 + 套餐内的检查项（按明细录入顺序返回）
     */
    @Override
    public SetmealDto getSetmealById(Integer smid) {
        if (smid == null) {
            return null;
        }
        Setmeal setmeal = baseMapper.selectById(smid);
        if (setmeal == null) {
            return null;
        }

        SetmealDto dto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, dto);
        dto.setItems(listItems(smid));
        return dto;
    }

    /**
     * 查询套餐包含的检查项：setmealdetailed -> checkitem
     */
    private List<CheckitemDto> listItems(Integer smid) {
        LambdaQueryWrapper<Setmealdetailed> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Setmealdetailed::getSmid, smid).orderByAsc(Setmealdetailed::getSdid);
        List<Setmealdetailed> details = setmealdetailedService.list(wrapper);

        List<CheckitemDto> items = new ArrayList<>();
        if (details.isEmpty()) {
            return items;
        }

        // 去重并保持套餐明细中的顺序
        Set<Integer> ciids = new LinkedHashSet<>();
        for (Setmealdetailed detail : details) {
            if (detail.getCiid() != null) {
                ciids.add(detail.getCiid());
            }
        }
        if (ciids.isEmpty()) {
            return items;
        }

        Map<Integer, Checkitem> checkitemMap = checkitemService.listByIds(ciids).stream()
                .collect(Collectors.toMap(Checkitem::getCiid, Function.identity(), (a, b) -> a));

        for (Integer ciid : ciids) {
            Checkitem checkitem = checkitemMap.get(ciid);
            if (checkitem == null) {
                continue;
            }
            CheckitemDto item = new CheckitemDto();
            BeanUtils.copyProperties(checkitem, item);
            items.add(item);
        }
        return items;
    }
}
