package com.it.xxhealth.service.impl;

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

}




