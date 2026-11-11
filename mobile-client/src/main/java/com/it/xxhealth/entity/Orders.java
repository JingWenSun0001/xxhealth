package com.it.xxhealth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 体检预约订单表
 * @TableName orders
 */
@TableName(value ="orders")
@Data
public class Orders {
    /**
     * 订单编号
     */
    @TableId(type = IdType.AUTO)
    private Integer orderid;

    /**
     * 预约日期
     */
    private Date orderdate;

    /**
     * 客户编号
     */
    private String userid;

    /**
     * 所属医院编号
     */
    private Integer hpid;

    /**
     * 所属套餐编号
     */
    private Integer smid;

    /**
     * 订单状态 (1:未归档; 2:已归档)
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
        Orders other = (Orders) that;
        return (this.getOrderid() == null ? other.getOrderid() == null : this.getOrderid().equals(other.getOrderid()))
            && (this.getOrderdate() == null ? other.getOrderdate() == null : this.getOrderdate().equals(other.getOrderdate()))
            && (this.getUserid() == null ? other.getUserid() == null : this.getUserid().equals(other.getUserid()))
            && (this.getHpid() == null ? other.getHpid() == null : this.getHpid().equals(other.getHpid()))
            && (this.getSmid() == null ? other.getSmid() == null : this.getSmid().equals(other.getSmid()))
            && (this.getState() == null ? other.getState() == null : this.getState().equals(other.getState()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getOrderid() == null) ? 0 : getOrderid().hashCode());
        result = prime * result + ((getOrderdate() == null) ? 0 : getOrderdate().hashCode());
        result = prime * result + ((getUserid() == null) ? 0 : getUserid().hashCode());
        result = prime * result + ((getHpid() == null) ? 0 : getHpid().hashCode());
        result = prime * result + ((getSmid() == null) ? 0 : getSmid().hashCode());
        result = prime * result + ((getState() == null) ? 0 : getState().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", orderid=").append(orderid);
        sb.append(", orderdate=").append(orderdate);
        sb.append(", userid=").append(userid);
        sb.append(", hpid=").append(hpid);
        sb.append(", smid=").append(smid);
        sb.append(", state=").append(state);
        sb.append("]");
        return sb.toString();
    }
}