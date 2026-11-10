package com.it.xxhealth.controller;

import com.it.xxhealth.entity.Hospital;
import com.it.xxhealth.service.HospitalService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端 - 医院接口（列表 / 详情）
 * <p>
 * 按文档口径：成功返回裸对象，失败返回 null 或空集合，不再包 Result。
 * 完整路径带 context-path：/tijian/hospital/...
 */
@RestController
@RequestMapping("/hospital")
public class HospitalController {

    @Autowired
    private HospitalService hospitalService;

    /**
     * 医院列表：按 state 过滤，state 为 null 查全部
     */
    @PostMapping("/listHospital")
    public List<Hospital> listHospital(@RequestBody(required = false) Hospital hospital) {
        Integer state = hospital == null ? null : hospital.getState();
        return hospitalService.listHospital(state);
    }

    /**
     * 医院详情：不存在返回 null
     */
    @PostMapping("/getHospitalById")
    public Hospital getHospitalById(@RequestBody Hospital hospital) {
        if (hospital == null || hospital.getHpid() == null) {
            return null;
        }
        return hospitalService.getHospitalById(hospital.getHpid());
    }
}
