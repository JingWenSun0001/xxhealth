package com.it.xxhealth.controller;

import com.it.xxhealth.common.Result;
import com.it.xxhealth.entity.Doctor;
import com.it.xxhealth.service.DoctorService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 旧版医生账号接口，提供医生编码和密码校验功能。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
@RestController
@RequestMapping("/doctor")
public class DoctorController {

    /**
     * 医生账号业务服务。
     */
    @Resource
    private DoctorService doctorService;

    /**
     * 根据医生编码和密码进行登录
     * @param doctor 医生登录信息
     * @return 查询到的业务数据；不存在时按接口约定返回 null 或空集合
     */
    @PostMapping("/getDoctorByCodeByPass")
    public Result<Doctor> getDoctorByCodeByPass(@RequestBody Doctor doctor) {
        Doctor loginDoctor = doctorService.getDoctorByCodeByPass(doctor);
        if (loginDoctor == null) {
            return Result.error(401, "医生编码或密码错误");
        }
        return Result.success(loginDoctor);
    }
}
