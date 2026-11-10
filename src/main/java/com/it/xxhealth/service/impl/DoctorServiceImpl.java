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

    /**
     * 修改医生资料（姓名 / 性别 / 科室）
     */
    @Override
    public int updateDoctor(Doctor doctor) {
        if (doctor == null || doctor.getDocid() == null) {
            return 0;
        }
        Doctor current = this.baseMapper.selectById(doctor.getDocid());
        if (current == null) {
            return 0;
        }
        Doctor update = new Doctor();
        update.setDocid(doctor.getDocid());
        update.setRealname(doctor.getRealname());
        update.setSex(doctor.getSex());
        update.setDeptno(doctor.getDeptno());
        this.baseMapper.updateById(update);
        // 资料未变化时受影响行数为 0，这里以“记录存在”为准
        return 1;
    }

    /**
     * 修改密码：原密码校验通过才更新
     *
     * @return 1 成功 / 0 原密码错误或参数不合法
     */
    @Override
    public int updatePassword(Integer docid, String oldPassword, String newPassword) {
        if (docid == null || oldPassword == null || oldPassword.isEmpty()
                || newPassword == null || newPassword.length() < 6) {
            return 0;
        }
        Doctor current = this.baseMapper.selectById(docid);
        if (current == null || !oldPassword.equals(current.getPassword())) {
            return 0;
        }
        Doctor update = new Doctor();
        update.setDocid(docid);
        update.setPassword(newPassword);
        this.baseMapper.updateById(update);
        // 新旧密码相同时受影响行数为 0，这里以“原密码校验通过”为准
        return 1;
    }

    /**
     * 医生注册：校验编码唯一 + 手机号格式
     *
     * @return 1 成功 / -1 编码已存在 / 0 参数不合法
     */
    @Override
    public int register(Doctor doctor) {
        if (doctor == null) {
            return 0;
        }
        String doccode = trimToEmpty(doctor.getDoccode());
        String realname = trimToEmpty(doctor.getRealname());
        String phone = trimToEmpty(doctor.getPhone());
        String password = doctor.getPassword();

        if (doccode.isEmpty() || doccode.length() < 3 || doccode.length() > 20) {
            return 0;
        }
        if (realname.isEmpty() || realname.length() > 20) {
            return 0;
        }
        if (!phone.matches("\\d{11}")) {
            return 0;
        }
        if (password == null || password.length() < 6 || password.length() > 20) {
            return 0;
        }

        Long exists = this.baseMapper.selectCount(
                new LambdaQueryWrapper<Doctor>().eq(Doctor::getDoccode, doccode));
        if (exists != null && exists > 0) {
            return -1;
        }

        Doctor entity = new Doctor();
        entity.setDoccode(doccode);
        entity.setRealname(realname);
        entity.setPhone(phone);
        entity.setPassword(password);
        entity.setSex(doctor.getSex() == null ? 1 : doctor.getSex());
        entity.setDeptno(doctor.getDeptno() == null ? 1 : doctor.getDeptno());
        return this.baseMapper.insert(entity) > 0 ? 1 : 0;
    }

    private String trimToEmpty(String text) {
        return text == null ? "" : text.trim();
    }
}
