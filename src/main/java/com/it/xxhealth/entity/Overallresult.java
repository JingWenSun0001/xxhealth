package com.it.xxhealth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 体检报告总检结论实体，对应 overallresult 表。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
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
        Overallresult other = (Overallresult) that;
        return (this.getOrid() == null ? other.getOrid() == null : this.getOrid().equals(other.getOrid()))
            && (this.getTitle() == null ? other.getTitle() == null : this.getTitle().equals(other.getTitle()))
            && (this.getContent() == null ? other.getContent() == null : this.getContent().equals(other.getContent()))
            && (this.getOrderid() == null ? other.getOrderid() == null : this.getOrderid().equals(other.getOrderid()));
    }

    /**
     * 根据业务字段计算对象哈希值。
     * @return 当前对象的哈希值
     */
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
        sb.append(", orid=").append(orid);
        sb.append(", title=").append(title);
        sb.append(", content=").append(content);
        sb.append(", orderid=").append(orderid);
        sb.append("]");
        return sb.toString();
    }
}