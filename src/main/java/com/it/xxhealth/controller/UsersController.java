package com.it.xxhealth.controller;

import com.it.xxhealth.entity.Users;
import com.it.xxhealth.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端 - 账号接口（登录 / 手机号查重 / 注册）
 * <p>
 * 按文档口径：成功返回裸对象，失败返回 null 或 0，不再包 Result。
 * 完整路径带 context-path：/tijian/users/...
 */
@RestController
@RequestMapping("/users")
public class UsersController {

    @Autowired
    private UsersService usersService;

    /**
     * 登录：手机号 + 密码，成功返回用户对象，账号或密码错误返回 null
     */
    @PostMapping("/getUsersByUserIdByPass")
    public Users getUsersByUserIdByPass(@RequestBody Users users) {
        if (users == null || isBlank(users.getUserid()) || isBlank(users.getPassword())) {
            return null;
        }
        return usersService.getUsersByUserIdByPass(users);
    }

    /**
     * 手机号查重：返回该用户对象，未注册返回 null
     */
    @PostMapping("/getUsersById")
    public Users getUsersById(@RequestBody Users users) {
        if (users == null || isBlank(users.getUserid())) {
            return null;
        }
        return usersService.getUsersById(users);
    }

    /**
     * 注册：返回 1 成功，0 手机号已存在或参数不合法
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

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
