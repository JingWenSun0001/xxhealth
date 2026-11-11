package com.it.xxhealth.service;

import com.it.xxhealth.entity.Users;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Ljz
* @description 针对表【users(用户表)】的数据库操作Service
* @createDate 2026-11-04 09:47:06
*/
public interface UsersService extends IService<Users> {

    /**
     * 登录校验：手机号 + 密码
     *
     * @param users 只用到 userid、password
     * @return 校验通过返回用户信息，手机号不存在或密码错误返回 null
     */
    Users getUsersByUserIdByPass(Users users);

    /**
     * 手机号查重
     *
     * @param users 只用到 userid
     * @return 该用户对象；未注册返回 null
     */
    Users getUsersById(Users users);

    /**
     * 注册（手机号重复时不写入）
     *
     * @param users 待注册用户
     * @return 1 注册成功；-1 手机号已注册；0 写入失败
     */
    int saveUsers(Users users);

}
