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

    /**
     * 修改医生资料（姓名 / 性别 / 科室）
     *
     * @return 影响行数
     */
    int updateDoctor(Doctor doctor);

    /**
     * 修改密码（校验原密码）
     *
     * @return 1 成功 / 0 原密码错误或参数不合法
     */
    int updatePassword(Integer docid, String oldPassword, String newPassword);

}
