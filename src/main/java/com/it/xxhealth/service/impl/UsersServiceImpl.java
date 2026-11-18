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
 * 用户账号业务实现。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users>
    implements UsersService{

    /**
     * 负责把用户和订单同步到医生工作台的服务。
     */
    private final MobileDoctorSyncService mobileDoctorSyncService;

    /**
     * 创建 UsersServiceImpl 实例并注入所需依赖。
     * @param mobileDoctorSyncService mobileDoctorSyncService 参数
     */
    public UsersServiceImpl(MobileDoctorSyncService mobileDoctorSyncService) {
        this.mobileDoctorSyncService = mobileDoctorSyncService;
    }

    /**
     * 根据手机号和密码校验用户登录信息。
     * @param users 用户账号数据
     * @return 查询到的业务数据；不存在时按接口约定返回 null 或空集合
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
     * 根据手机号查询用户账号。
     * @param users 用户账号数据
     * @return 查询到的业务数据；不存在时按接口约定返回 null 或空集合
     */
    @Override
    public Users getUsersById(Users users) {
        if (users == null || isBlank(users.getUserid())) {
            return null;
        }
        return getByUserid(users.getUserid());
    }

    /**
     * 校验并注册用户，同时同步医生端患者档案。
     * @param users 用户账号数据
     * @return 处理结果数值
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
     * @param userid 用户手机号
     * @return 查询到的业务数据；不存在时按接口约定返回 null 或空集合
     */
    private Users getByUserid(String userid) {
        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Users::getUserid, userid);
        return baseMapper.selectOne(wrapper);
    }

    /**
     * 执行 isBlank 对应的业务处理。
     * @param text text 参数
     * @return 条件成立时返回 true，否则返回 false
     */
    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
