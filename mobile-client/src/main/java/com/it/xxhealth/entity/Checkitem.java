package com.it.xxhealth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 检查项信息表
 * @TableName checkitem
 */
@TableName(value ="checkitem")
@Data
public class Checkitem {
    /**
     * 检查项编号
     */
    @TableId(type = IdType.AUTO)
    private Integer ciid;

    /**
     * 检查项名称
     */
    private String ciname;

    /**
     * 检查项内容
     */
    private String cicontent;

    /**
     * 检查项意义
     */
    private String meaning;

    /**
     * 备注
     */
    private String remarks;

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
        Checkitem other = (Checkitem) that;
        return (this.getCiid() == null ? other.getCiid() == null : this.getCiid().equals(other.getCiid()))
            && (this.getCiname() == null ? other.getCiname() == null : this.getCiname().equals(other.getCiname()))
            && (this.getCicontent() == null ? other.getCicontent() == null : this.getCicontent().equals(other.getCicontent()))
            && (this.getMeaning() == null ? other.getMeaning() == null : this.getMeaning().equals(other.getMeaning()))
            && (this.getRemarks() == null ? other.getRemarks() == null : this.getRemarks().equals(other.getRemarks()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getCiid() == null) ? 0 : getCiid().hashCode());
        result = prime * result + ((getCiname() == null) ? 0 : getCiname().hashCode());
        result = prime * result + ((getCicontent() == null) ? 0 : getCicontent().hashCode());
        result = prime * result + ((getMeaning() == null) ? 0 : getMeaning().hashCode());
        result = prime * result + ((getRemarks() == null) ? 0 : getRemarks().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", ciid=").append(ciid);
        sb.append(", ciname=").append(ciname);
        sb.append(", cicontent=").append(cicontent);
        sb.append(", meaning=").append(meaning);
        sb.append(", remarks=").append(remarks);
        sb.append("]");
        return sb.toString();
    }
}