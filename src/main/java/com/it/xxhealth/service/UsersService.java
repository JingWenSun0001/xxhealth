package com.it.xxhealth.service;

import com.it.xxhealth.entity.Users;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户账号业务接口。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
public interface UsersService extends IService<Users> {

    /**
     * 根据手机号和密码校验用户登录信息。
     * @param users 用户账号数据
     * @return 查询到的业务数据；不存在时按接口约定返回 null 或空集合
     */
    Users getUsersByUserIdByPass(Users users);

    /**
     * 根据手机号查询用户账号。
     * @param users 用户账号数据
     * @return 查询到的业务数据；不存在时按接口约定返回 null 或空集合
     */
    Users getUsersById(Users users);

    /**
     * 校验并注册用户，同时同步医生端患者档案。
     * @param users 用户账号数据
     * @return 处理结果数值
     */
    int saveUsers(Users users);

}
