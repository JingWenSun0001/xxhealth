package com.it.xxhealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.xxhealth.entity.Hospital;
import com.it.xxhealth.service.HospitalService;
import com.it.xxhealth.mapper.HospitalMapper;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 医院信息表 Service 实现
 */
@Service
public class HospitalServiceImpl extends ServiceImpl<HospitalMapper, Hospital>
    implements HospitalService {

    /**
     * 医院列表：state 为 null 时查全部
     */
    @Override
    public List<Hospital> listHospital(Integer state) {
        LambdaQueryWrapper<Hospital> wrapper = new LambdaQueryWrapper<>();
        if (state != null) {
            wrapper.eq(Hospital::getState, state);
        }
        wrapper.orderByAsc(Hospital::getHpid);
        return baseMapper.selectList(wrapper);
    }

    /**
     * 医院详情：不存在返回 null
     */
    @Override
    public Hospital getHospitalById(Integer hpid) {
        if (hpid == null) {
            return null;
        }
        return baseMapper.selectById(hpid);
    }
}
