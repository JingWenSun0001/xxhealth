package com.it.xxhealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.xxhealth.entity.Hospital;
import com.it.xxhealth.service.HospitalService;
import com.it.xxhealth.mapper.HospitalMapper;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 体检机构业务实现。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
@Service
public class HospitalServiceImpl extends ServiceImpl<HospitalMapper, Hospital>
    implements HospitalService {

    /**
     * 按营业状态查询体检机构列表。
     * @param state 目标业务状态
     * @return 符合条件的数据集合
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
     * 根据机构编号查询体检机构详情。
     * @param hpid 体检机构编号
     * @return 查询到的业务数据；不存在时按接口约定返回 null 或空集合
     */
    @Override
    public Hospital getHospitalById(Integer hpid) {
        if (hpid == null) {
            return null;
        }
        return baseMapper.selectById(hpid);
    }
}
