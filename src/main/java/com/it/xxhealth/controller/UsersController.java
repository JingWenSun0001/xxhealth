package com.it.xxhealth.controller;

import com.it.xxhealth.entity.Users;
import com.it.xxhealth.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户账号接口，提供登录、账号查询和注册功能。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
@RestController
@RequestMapping("/users")
public class UsersController {

    /**
     * 用户账号业务服务。
     */
    @Autowired
    private UsersService usersService;

    /**
     * 根据手机号和密码校验用户登录信息。
     * @param users 用户账号数据
     * @return 查询到的业务数据；不存在时按接口约定返回 null 或空集合
     */
    @PostMapping("/getUsersByUserIdByPass")
    public Users getUsersByUserIdByPass(@RequestBody Users users) {
        if (users == null || isBlank(users.getUserid()) || isBlank(users.getPassword())) {
            return null;
        }
        return usersService.getUsersByUserIdByPass(users);
    }

    /**
     * 根据手机号查询用户账号。
     * @param users 用户账号数据
     * @return 查询到的业务数据；不存在时按接口约定返回 null 或空集合
     */
    @PostMapping("/getUsersById")
    public Users getUsersById(@RequestBody Users users) {
        if (users == null || isBlank(users.getUserid())) {
            return null;
        }
        return usersService.getUsersById(users);
    }

    /**
     * 校验并注册用户，同时同步医生端患者档案。
     * @param users 用户账号数据
     * @return 处理结果数值
     */
    @PostMapping("/saveUsers")
    public int saveUsers(@RequestBody Users users) {
        if (users == null || isBlank(users.getUserid()) || isBlank(users.getPassword())
                || isBlank(users.getRealname())) {
            return 0;
        }
        if (!users.getUserid().trim().matches("\\d{11}")) {
            return 0;
        }
        if (users.getSex() == null || isBlank(users.getIdentitycard()) || users.getBirthday() == null) {
            return 0;
        }
        if (!users.getIdentitycard().trim().matches("\\d{17}[\\dXx]")) {
            return 0;
        }
        // 未传用户类型时按普通用户处理
        if (users.getUsertype() == null) {
            users.setUsertype(1);
        }

        int rows = usersService.saveUsers(users);
        return rows > 0 ? 1 : 0;
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
