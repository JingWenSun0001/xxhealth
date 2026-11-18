package com.it.xxhealth.service;

import com.it.xxhealth.entity.Doctor;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 医生账号业务接口。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
public interface DoctorService extends IService<Doctor> {

    /**
     * 根据医生编码和密码进行登录验证
     * @param doctor 医生登录信息
     * @return 查询到的业务数据；不存在时按接口约定返回 null 或空集合
     */
    Doctor getDoctorByCodeByPass(Doctor doctor);

}
