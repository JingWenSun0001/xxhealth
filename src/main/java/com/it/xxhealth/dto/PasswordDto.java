package com.it.xxhealth.dto;

import lombok.Data;

/**
 * 修改密码参数
 */
@Data
public class PasswordDto {

    private Integer docid;

    private String oldPassword;

    private String newPassword;
}
