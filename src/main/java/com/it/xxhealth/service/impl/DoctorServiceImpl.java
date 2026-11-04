package com.it.xxhealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.xxhealth.entity.Doctor;
import com.it.xxhealth.service.DoctorService;
import com.it.xxhealth.mapper.DoctorMapper;
import org.springframework.stereotype.Service;

/**
* @author Ljz
* @description 针对表【doctor(医生信息表)】的数据库操作Service实现
* @createDate 2026-11-04 09:47:06
*/
@Service
public class DoctorServiceImpl extends ServiceImpl<DoctorMapper, Doctor>
    implements DoctorService{

    /**
     * 根据医生编码 + 密码查询医生信息（登录校验）。
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
