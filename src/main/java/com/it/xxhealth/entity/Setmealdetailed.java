package com.it.xxhealth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 套餐与检查项目的关联实体，对应 setmealdetailed 表。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
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
        Setmealdetailed other = (Setmealdetailed) that;
        return (this.getSdid() == null ? other.getSdid() == null : this.getSdid().equals(other.getSdid()))
            && (this.getSmid() == null ? other.getSmid() == null : this.getSmid().equals(other.getSmid()))
            && (this.getCiid() == null ? other.getCiid() == null : this.getCiid().equals(other.getCiid()));
    }

    /**
     * 根据业务字段计算对象哈希值。
     * @return 当前对象的哈希值
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getSdid() == null) ? 0 : getSdid().hashCode());
        result = prime * result + ((getSmid() == null) ? 0 : getSmid().hashCode());
        result = prime * result + ((getCiid() == null) ? 0 : getCiid().hashCode());
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
        sb.append(", sdid=").append(sdid);
        sb.append(", smid=").append(smid);
        sb.append(", ciid=").append(ciid);
        sb.append("]");
        return sb.toString();
    }
}