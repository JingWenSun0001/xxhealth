package com.it.xxhealth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 体检检查项目实体，对应 checkitem 表。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
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
        Checkitem other = (Checkitem) that;
        return (this.getCiid() == null ? other.getCiid() == null : this.getCiid().equals(other.getCiid()))
            && (this.getCiname() == null ? other.getCiname() == null : this.getCiname().equals(other.getCiname()))
            && (this.getCicontent() == null ? other.getCicontent() == null : this.getCicontent().equals(other.getCicontent()))
            && (this.getMeaning() == null ? other.getMeaning() == null : this.getMeaning().equals(other.getMeaning()))
            && (this.getRemarks() == null ? other.getRemarks() == null : this.getRemarks().equals(other.getRemarks()));
    }

    /**
     * 根据业务字段计算对象哈希值。
     * @return 当前对象的哈希值
     */
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
        sb.append(", ciid=").append(ciid);
        sb.append(", ciname=").append(ciname);
        sb.append(", cicontent=").append(cicontent);
        sb.append(", meaning=").append(meaning);
        sb.append(", remarks=").append(remarks);
        sb.append("]");
        return sb.toString();
    }
}