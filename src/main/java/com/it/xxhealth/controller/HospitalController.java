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
 * 体检机构接口，提供机构列表和详情查询功能。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
@RestController
@RequestMapping("/hospital")
public class HospitalController {

    /**
     * 体检机构业务服务。
     */
    @Autowired
    private HospitalService hospitalService;

    /**
     * 按营业状态查询体检机构列表。
     * @param hospital 体检机构查询条件
     * @return 符合条件的数据集合
     */
    @PostMapping("/listHospital")
    public List<Hospital> listHospital(@RequestBody(required = false) Hospital hospital) {
        Integer state = hospital == null ? null : hospital.getState();
        return hospitalService.listHospital(state);
    }

    /**
     * 根据机构编号查询体检机构详情。
     * @param hospital 体检机构查询条件
     * @return 查询到的业务数据；不存在时按接口约定返回 null 或空集合
     */
    @PostMapping("/getHospitalById")
    public Hospital getHospitalById(@RequestBody Hospital hospital) {
        if (hospital == null || hospital.getHpid() == null) {
            return null;
        }
        return hospitalService.getHospitalById(hospital.getHpid());
    }
}
