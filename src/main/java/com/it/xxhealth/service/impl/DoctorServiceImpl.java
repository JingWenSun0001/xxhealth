package com.it.xxhealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.xxhealth.entity.Doctor;
import com.it.xxhealth.service.DoctorService;
import com.it.xxhealth.mapper.DoctorMapper;
import org.springframework.stereotype.Service;

/**
 * 医生账号业务实现。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
@Service
public class DoctorServiceImpl extends ServiceImpl<DoctorMapper, Doctor>
    implements DoctorService{

    /**
     * 根据医生编码 + 密码查询医生信息（登录校验）。
     * @param doctor 医生登录信息
     * @return 查询到的业务数据；不存在时按接口约定返回 null 或空集合
     */
    @Override
    public Doctor getDoctorByCodeByPass(Doctor doctor) {
        if (doctor == null
                || doctor.getDoccode() == null || doctor.getDoccode().trim().isEmpty()
                || doctor.getPassword() == null || doctor.getPassword().isEmpty()) {
            return null;
        }
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Doctor::getDoccode, doctor.getDoccode().trim())
                .eq(Doctor::getPassword, doctor.getPassword());
        // 编码唯一，取一条即可；false 表示查不到不抛异常
        return this.getOne(wrapper, false);
    }
}
