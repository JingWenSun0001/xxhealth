package com.it.xxhealth.service;

import com.it.xxhealth.entity.Hospital;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * 体检机构业务接口。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
public interface HospitalService extends IService<Hospital> {

    /**
     * 按营业状态查询体检机构列表。
     * @param state 目标业务状态
     * @return 符合条件的数据集合
     */
    List<Hospital> listHospital(Integer state);

    /**
     * 根据机构编号查询体检机构详情。
     * @param hpid 体检机构编号
     * @return 查询到的业务数据；不存在时按接口约定返回 null 或空集合
     */
    Hospital getHospitalById(Integer hpid);
}
