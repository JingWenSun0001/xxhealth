package com.it.xxhealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.xxhealth.entity.Users;
import com.it.xxhealth.doctor.MobileDoctorSyncService;
import com.it.xxhealth.service.UsersService;
import com.it.xxhealth.mapper.UsersMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* @author Ljz
* @description 针对表【users(用户表)】的数据库操作Service实现
* @createDate 2026-11-04 09:47:06
*/
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users>
    implements UsersService{

    private final MobileDoctorSyncService mobileDoctorSyncService;

    public UsersServiceImpl(MobileDoctorSyncService mobileDoctorSyncService) {
        this.mobileDoctorSyncService = mobileDoctorSyncService;
    }

    /**
     * 登录：先按手机号取用户，再比对密码（密码明文存储，与原表设计保持一致）
     */
    @Override
    public Users getUsersByUserIdByPass(Users users) {
        if (users == null || isBlank(users.getUserid())) {
            return null;
        }
        Users db = getByUserid(users.getUserid());
        if (db == null) {
            return null;
        }
        if (db.getPassword() == null || !db.getPassword().equals(users.getPassword())) {
            return null;
        }
        return db;
    }

    /**
     * 手机号查重：未注册返回 null
     */
    @Override
    public Users getUsersById(Users users) {
        if (users == null || isBlank(users.getUserid())) {
            return null;
        }
        return getByUserid(users.getUserid());
    }

    /**
     * 注册：手机号已存在则不写入
     */
    @Override
    @Transactional
    public int saveUsers(Users users) {
        if (users == null || isBlank(users.getUserid())) {
            return 0;
        }
        if (getByUserid(users.getUserid()) != null) {
            return -1;
        }
        int rows = baseMapper.insert(users);
        if (rows > 0) {
            mobileDoctorSyncService.syncUser(users.getUserid());
        }
        return rows;
    }

    /**
     * 按手机号（主键 userId）查询用户
     */
    private Users getByUserid(String userid) {
        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Users::getUserid, userid);
        return baseMapper.selectOne(wrapper);
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
