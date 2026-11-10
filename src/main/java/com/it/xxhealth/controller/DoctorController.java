package com.it.xxhealth.controller;

import com.it.xxhealth.common.Result;
import com.it.xxhealth.dto.PasswordDto;
import com.it.xxhealth.entity.Doctor;
import com.it.xxhealth.service.DoctorService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 医生端：医生登录接口
 */
@RestController
@RequestMapping("/doctor")
public class DoctorController {

    @Resource
    private DoctorService doctorService;

    /**
     * 根据医生编码和密码进行登录
     *
     * @param doctor 包含 doccode 与 password 的医生对象
     * @return 登录成功返回医生信息
     */
    @PostMapping("/getDoctorByCodeByPass")
    public Result<Doctor> getDoctorByCodeByPass(@RequestBody Doctor doctor) {
        Doctor loginDoctor = doctorService.getDoctorByCodeByPass(doctor);
        if (loginDoctor == null) {
            return Result.error(401, "医生编码或密码错误");
        }
        return Result.success(loginDoctor);
    }

    /**
     * 修改医生资料（姓名 / 性别 / 科室）
     */
    @PostMapping("/updateDoctor")
    public Result<Integer> updateDoctor(@RequestBody Doctor doctor) {
        if (doctor == null || doctor.getDocid() == null) {
            return Result.error(400, "医生编号不能为空");
        }
        int rows = doctorService.updateDoctor(doctor);
        if (rows <= 0) {
            return Result.error(500, "资料更新失败");
        }
        return Result.success(rows);
    }

    /**
     * 修改密码（校验原密码）
     */
    @PostMapping("/updatePassword")
    public Result<Integer> updatePassword(@RequestBody PasswordDto dto) {
        if (dto == null || dto.getDocid() == null) {
            return Result.error(400, "医生编号不能为空");
        }
        int rows = doctorService.updatePassword(dto.getDocid(), dto.getOldPassword(), dto.getNewPassword());
        if (rows <= 0) {
            return Result.error(400, "原密码不正确，或新密码不符合要求");
        }
        return Result.success(rows);
    }
}
