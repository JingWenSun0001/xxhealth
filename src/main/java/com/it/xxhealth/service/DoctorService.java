package com.it.xxhealth.service;

import com.it.xxhealth.entity.Doctor;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Ljz
* @description 针对表【doctor(医生信息表)】的数据库操作Service
* @createDate 2026-11-04 09:47:06
*/
public interface DoctorService extends IService<Doctor> {

    /**
     * 根据医生编码和密码进行登录验证
     *
     * @param doctor 包含 doccode（医生编码）和 password（密码）的医生对象
     * @return 匹配到的医生信息；编码或密码为空、匹配不到时返回 null
     */
    Doctor getDoctorByCodeByPass(Doctor doctor);

}
