package com.it.xxhealth.service;

import com.it.xxhealth.entity.Hospital;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * 医院信息表 Service
 */
public interface HospitalService extends IService<Hospital> {

    /**
     * 医院列表
     *
     * @param state 医院状态：1 正常 / 2 其他；传 null 查全部
     * @return 医院列表，无数据时返回空集合
     */
    List<Hospital> listHospital(Integer state);

    /**
     * 医院详情
     *
     * @param hpid 医院编号
     * @return 医院对象，不存在时返回 null
     */
    Hospital getHospitalById(Integer hpid);
}
