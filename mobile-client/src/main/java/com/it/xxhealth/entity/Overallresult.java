package com.it.xxhealth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 总检结论信息表
 * @TableName overallresult
 */
@TableName(value ="overallresult")
@Data
public class Overallresult {
    /**
     * 总检结论项编号
     */
    @TableId(type = IdType.AUTO)
    private Integer orid;

    /**
     * 总检结论项标题
     */
    private String title;

    /**
     * 总检结论项内容
     */
    private String content;

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
        Overallresult other = (Overallresult) that;
        return (this.getOrid() == null ? other.getOrid() == null : this.getOrid().equals(other.getOrid()))
            && (this.getTitle() == null ? other.getTitle() == null : this.getTitle().equals(other.getTitle()))
            && (this.getContent() == null ? other.getContent() == null : this.getContent().equals(other.getContent()))
            && (this.getOrderid() == null ? other.getOrderid() == null : this.getOrderid().equals(other.getOrderid()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getOrid() == null) ? 0 : getOrid().hashCode());
        result = prime * result + ((getTitle() == null) ? 0 : getTitle().hashCode());
        result = prime * result + ((getContent() == null) ? 0 : getContent().hashCode());
        result = prime * result + ((getOrderid() == null) ? 0 : getOrderid().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", orid=").append(orid);
        sb.append(", title=").append(title);
        sb.append(", content=").append(content);
        sb.append(", orderid=").append(orderid);
        sb.append("]");
        return sb.toString();
    }
}