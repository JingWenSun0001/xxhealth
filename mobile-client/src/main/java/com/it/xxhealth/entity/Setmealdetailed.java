package com.it.xxhealth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 体检套餐项目明细表
 * @TableName setmealdetailed
 */
@TableName(value ="setmealdetailed")
@Data
public class Setmealdetailed {
    /**
     * 体检套餐项目明细编号
     */
    @TableId(type = IdType.AUTO)
    private Integer sdid;

    /**
     * 体检套餐编号
     */
    private Integer smid;

    /**
     * 体检检查项编号
     */
    private Integer ciid;

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
        Setmealdetailed other = (Setmealdetailed) that;
        return (this.getSdid() == null ? other.getSdid() == null : this.getSdid().equals(other.getSdid()))
            && (this.getSmid() == null ? other.getSmid() == null : this.getSmid().equals(other.getSmid()))
            && (this.getCiid() == null ? other.getCiid() == null : this.getCiid().equals(other.getCiid()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getSdid() == null) ? 0 : getSdid().hashCode());
        result = prime * result + ((getSmid() == null) ? 0 : getSmid().hashCode());
        result = prime * result + ((getCiid() == null) ? 0 : getCiid().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", sdid=").append(sdid);
        sb.append(", smid=").append(smid);
        sb.append(", ciid=").append(ciid);
        sb.append("]");
        return sb.toString();
    }
}