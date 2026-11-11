package com.it.xxhealth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 体检报告检查项信息表
 * @TableName cireport
 */
@TableName(value ="cireport")
@Data
public class Cireport {
    /**
     * 检查项报告主键
     */
    @TableId(type = IdType.AUTO)
    private Integer cirid;

    /**
     * 检查项编号
     */
    private Integer ciid;

    /**
     * 检查项名称
     */
    private String ciname;

    /**
     * 所属预约编号
     */
    private Integer orderid;

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
        Cireport other = (Cireport) that;
        return (this.getCirid() == null ? other.getCirid() == null : this.getCirid().equals(other.getCirid()))
            && (this.getCiid() == null ? other.getCiid() == null : this.getCiid().equals(other.getCiid()))
            && (this.getCiname() == null ? other.getCiname() == null : this.getCiname().equals(other.getCiname()))
            && (this.getOrderid() == null ? other.getOrderid() == null : this.getOrderid().equals(other.getOrderid()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getCirid() == null) ? 0 : getCirid().hashCode());
        result = prime * result + ((getCiid() == null) ? 0 : getCiid().hashCode());
        result = prime * result + ((getCiname() == null) ? 0 : getCiname().hashCode());
        result = prime * result + ((getOrderid() == null) ? 0 : getOrderid().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", cirid=").append(cirid);
        sb.append(", ciid=").append(ciid);
        sb.append(", ciname=").append(ciname);
        sb.append(", orderid=").append(orderid);
        sb.append("]");
        return sb.toString();
    }
}