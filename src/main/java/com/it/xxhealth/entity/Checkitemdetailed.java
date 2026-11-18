package com.it.xxhealth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 检查项目明细实体，对应 checkitemdetailed 表。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
@TableName(value ="checkitemdetailed")
@Data
public class Checkitemdetailed {
    /**
     * 检查项明细编号
     */
    @TableId(type = IdType.AUTO)
    private Integer cdid;

    /**
     * 检查项明细名称
     */
    private String name;

    /**
     * 检查项明细单位
     */
    private String unit;

    /**
     * 检查项明细正常值范围中的最小值
     */
    private Double minrange;

    /**
     * 检查项明细正常值范围中的最大值
     */
    private Double maxrange;

    /**
     * 检查项明细正常值 (非数字型)
     */
    private String normalvalue;

    /**
     * 检查项验证范围说明文字
     */
    private String normalvaluestring;

    /**
     * 检查项明细类型 (1:数值范围验证型; 2:数值相等验证型; 3:无需验证型; 4:描述型; 5:其它)
     */
    private Integer type;

    /**
     * 所属检查项编号
     */
    private Integer ciid;

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
        Checkitemdetailed other = (Checkitemdetailed) that;
        return (this.getCdid() == null ? other.getCdid() == null : this.getCdid().equals(other.getCdid()))
            && (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()))
            && (this.getUnit() == null ? other.getUnit() == null : this.getUnit().equals(other.getUnit()))
            && (this.getMinrange() == null ? other.getMinrange() == null : this.getMinrange().equals(other.getMinrange()))
            && (this.getMaxrange() == null ? other.getMaxrange() == null : this.getMaxrange().equals(other.getMaxrange()))
            && (this.getNormalvalue() == null ? other.getNormalvalue() == null : this.getNormalvalue().equals(other.getNormalvalue()))
            && (this.getNormalvaluestring() == null ? other.getNormalvaluestring() == null : this.getNormalvaluestring().equals(other.getNormalvaluestring()))
            && (this.getType() == null ? other.getType() == null : this.getType().equals(other.getType()))
            && (this.getCiid() == null ? other.getCiid() == null : this.getCiid().equals(other.getCiid()))
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
        result = prime * result + ((getCdid() == null) ? 0 : getCdid().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getUnit() == null) ? 0 : getUnit().hashCode());
        result = prime * result + ((getMinrange() == null) ? 0 : getMinrange().hashCode());
        result = prime * result + ((getMaxrange() == null) ? 0 : getMaxrange().hashCode());
        result = prime * result + ((getNormalvalue() == null) ? 0 : getNormalvalue().hashCode());
        result = prime * result + ((getNormalvaluestring() == null) ? 0 : getNormalvaluestring().hashCode());
        result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
        result = prime * result + ((getCiid() == null) ? 0 : getCiid().hashCode());
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
        sb.append(", cdid=").append(cdid);
        sb.append(", name=").append(name);
        sb.append(", unit=").append(unit);
        sb.append(", minrange=").append(minrange);
        sb.append(", maxrange=").append(maxrange);
        sb.append(", normalvalue=").append(normalvalue);
        sb.append(", normalvaluestring=").append(normalvaluestring);
        sb.append(", type=").append(type);
        sb.append(", ciid=").append(ciid);
        sb.append(", remarks=").append(remarks);
        sb.append("]");
        return sb.toString();
    }
}