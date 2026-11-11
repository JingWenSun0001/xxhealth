package com.it.xxhealth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 医生信息表
 * @TableName doctor
 */
@TableName(value ="doctor")
@Data
public class Doctor {
    /**
     * 医生编号
     */
    @TableId(type = IdType.AUTO)
    private Integer docid;

    /**
     * 医生编码 (登录用)
     */
    private String doccode;

    /**
     * 真实姓名
     */
    private String realname;

    /**
     * 密码 (登录用)
     */
    private String password;

    /**
     * 性别
     */
    private Integer sex;

    /**
     * 所属科室 (1:检验科; 2:内科; 3:外科)
     */
    private Integer deptno;

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
        Doctor other = (Doctor) that;
        return (this.getDocid() == null ? other.getDocid() == null : this.getDocid().equals(other.getDocid()))
            && (this.getDoccode() == null ? other.getDoccode() == null : this.getDoccode().equals(other.getDoccode()))
            && (this.getRealname() == null ? other.getRealname() == null : this.getRealname().equals(other.getRealname()))
            && (this.getPassword() == null ? other.getPassword() == null : this.getPassword().equals(other.getPassword()))
            && (this.getSex() == null ? other.getSex() == null : this.getSex().equals(other.getSex()))
            && (this.getDeptno() == null ? other.getDeptno() == null : this.getDeptno().equals(other.getDeptno()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getDocid() == null) ? 0 : getDocid().hashCode());
        result = prime * result + ((getDoccode() == null) ? 0 : getDoccode().hashCode());
        result = prime * result + ((getRealname() == null) ? 0 : getRealname().hashCode());
        result = prime * result + ((getPassword() == null) ? 0 : getPassword().hashCode());
        result = prime * result + ((getSex() == null) ? 0 : getSex().hashCode());
        result = prime * result + ((getDeptno() == null) ? 0 : getDeptno().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", docid=").append(docid);
        sb.append(", doccode=").append(doccode);
        sb.append(", realname=").append(realname);
        sb.append(", password=").append(password);
        sb.append(", sex=").append(sex);
        sb.append(", deptno=").append(deptno);
        sb.append("]");
        return sb.toString();
    }
}