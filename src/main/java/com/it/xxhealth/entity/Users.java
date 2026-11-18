package com.it.xxhealth.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 用户账号实体，对应 users 表。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
@TableName(value ="users")
@Data
public class Users {
    /**
     * 用户编号 (手机号码)
     */
    @TableId
    private String userid;

    /**
     * 密码
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realname;

    /**
     * 用户性别 (1:男; 0:女)
     */
    private Integer sex;

    /**
     * 身份证号
     */
    private String identitycard;

    /**
     * 出生日期
     */
    private Date birthday;

    /**
     * 用户类型 (1:普通用户; 2:东软内部员工; 3:其他)
     */
    private Integer usertype;

    /**
     * 比较当前对象与目标对象的业务字段是否相等。
     * @param that 待比较的目标对象
     * @return 两个对象的业务字段相等时返回 true，否则返回 false
     */
    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Users other = (Users) that;
        return (this.getUserid() == null ? other.getUserid() == null : this.getUserid().equals(other.getUserid()))
            && (this.getPassword() == null ? other.getPassword() == null : this.getPassword().equals(other.getPassword()))
            && (this.getRealname() == null ? other.getRealname() == null : this.getRealname().equals(other.getRealname()))
            && (this.getSex() == null ? other.getSex() == null : this.getSex().equals(other.getSex()))
            && (this.getIdentitycard() == null ? other.getIdentitycard() == null : this.getIdentitycard().equals(other.getIdentitycard()))
            && (this.getBirthday() == null ? other.getBirthday() == null : this.getBirthday().equals(other.getBirthday()))
            && (this.getUsertype() == null ? other.getUsertype() == null : this.getUsertype().equals(other.getUsertype()));
    }

    /**
     * 根据业务字段计算对象哈希值。
     * @return 当前对象的哈希值
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getUserid() == null) ? 0 : getUserid().hashCode());
        result = prime * result + ((getPassword() == null) ? 0 : getPassword().hashCode());
        result = prime * result + ((getRealname() == null) ? 0 : getRealname().hashCode());
        result = prime * result + ((getSex() == null) ? 0 : getSex().hashCode());
        result = prime * result + ((getIdentitycard() == null) ? 0 : getIdentitycard().hashCode());
        result = prime * result + ((getBirthday() == null) ? 0 : getBirthday().hashCode());
        result = prime * result + ((getUsertype() == null) ? 0 : getUsertype().hashCode());
        return result;
    }

    /**
     * 生成便于日志和调试查看的对象字符串。
     * @return 包含主要字段的对象描述文本
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", userid=").append(userid);
        sb.append(", password=").append(password);
        sb.append(", realname=").append(realname);
        sb.append(", sex=").append(sex);
        sb.append(", identitycard=").append(identitycard);
        sb.append(", birthday=").append(birthday);
        sb.append(", usertype=").append(usertype);
        sb.append("]");
        return sb.toString();
    }
}