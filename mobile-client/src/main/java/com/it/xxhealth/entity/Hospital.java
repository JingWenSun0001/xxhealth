package com.it.xxhealth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 医院信息表
 * @TableName hospital
 */
@TableName(value ="hospital")
@Data
public class Hospital {
    /**
     * 医院编号
     */
    @TableId(type = IdType.AUTO)
    private Integer hpid;

    /**
     * 医院名称
     */
    private String name;

    /**
     * 医院图片
     */
    private String picture;

    /**
     * 医院电话
     */
    private String telephone;

    /**
     * 医院地址
     */
    private String address;

    /**
     * 营业时间
     */
    private String businesshours;

    /**
     * 采血截止时间
     */
    private String deadline;

    /**
     * 预约人数规则
     */
    private String rule;

    /**
     * 医院状态 (1:正常; 2:其他)
     */
    private Integer state;

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
        Hospital other = (Hospital) that;
        return (this.getHpid() == null ? other.getHpid() == null : this.getHpid().equals(other.getHpid()))
            && (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()))
            && (this.getPicture() == null ? other.getPicture() == null : this.getPicture().equals(other.getPicture()))
            && (this.getTelephone() == null ? other.getTelephone() == null : this.getTelephone().equals(other.getTelephone()))
            && (this.getAddress() == null ? other.getAddress() == null : this.getAddress().equals(other.getAddress()))
            && (this.getBusinesshours() == null ? other.getBusinesshours() == null : this.getBusinesshours().equals(other.getBusinesshours()))
            && (this.getDeadline() == null ? other.getDeadline() == null : this.getDeadline().equals(other.getDeadline()))
            && (this.getRule() == null ? other.getRule() == null : this.getRule().equals(other.getRule()))
            && (this.getState() == null ? other.getState() == null : this.getState().equals(other.getState()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getHpid() == null) ? 0 : getHpid().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getPicture() == null) ? 0 : getPicture().hashCode());
        result = prime * result + ((getTelephone() == null) ? 0 : getTelephone().hashCode());
        result = prime * result + ((getAddress() == null) ? 0 : getAddress().hashCode());
        result = prime * result + ((getBusinesshours() == null) ? 0 : getBusinesshours().hashCode());
        result = prime * result + ((getDeadline() == null) ? 0 : getDeadline().hashCode());
        result = prime * result + ((getRule() == null) ? 0 : getRule().hashCode());
        result = prime * result + ((getState() == null) ? 0 : getState().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", hpid=").append(hpid);
        sb.append(", name=").append(name);
        sb.append(", picture=").append(picture);
        sb.append(", telephone=").append(telephone);
        sb.append(", address=").append(address);
        sb.append(", businesshours=").append(businesshours);
        sb.append(", deadline=").append(deadline);
        sb.append(", rule=").append(rule);
        sb.append(", state=").append(state);
        sb.append("]");
        return sb.toString();
    }
}