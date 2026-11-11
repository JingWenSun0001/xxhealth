package com.it.xxhealth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 体检报告检查项明细表
 * @TableName cidetailedreport
 */
@TableName(value ="cidetailedreport")
@Data
public class Cidetailedreport {
    /**
     * 检查项明细报告编号
     */
    @TableId(type = IdType.AUTO)
    private Integer cidrid;

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
     * 检查项目明细值
     */
    private String value;

    /**
     * 此项是否异常 (0:无异常; 1:异常)
     */
    private Integer iserror;

    /**
     * 所属检查项报告编号
     */
    private Integer ciid;

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
        Cidetailedreport other = (Cidetailedreport) that;
        return (this.getCidrid() == null ? other.getCidrid() == null : this.getCidrid().equals(other.getCidrid()))
            && (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()))
            && (this.getUnit() == null ? other.getUnit() == null : this.getUnit().equals(other.getUnit()))
            && (this.getMinrange() == null ? other.getMinrange() == null : this.getMinrange().equals(other.getMinrange()))
            && (this.getMaxrange() == null ? other.getMaxrange() == null : this.getMaxrange().equals(other.getMaxrange()))
            && (this.getNormalvalue() == null ? other.getNormalvalue() == null : this.getNormalvalue().equals(other.getNormalvalue()))
            && (this.getNormalvaluestring() == null ? other.getNormalvaluestring() == null : this.getNormalvaluestring().equals(other.getNormalvaluestring()))
            && (this.getType() == null ? other.getType() == null : this.getType().equals(other.getType()))
            && (this.getValue() == null ? other.getValue() == null : this.getValue().equals(other.getValue()))
            && (this.getIserror() == null ? other.getIserror() == null : this.getIserror().equals(other.getIserror()))
            && (this.getCiid() == null ? other.getCiid() == null : this.getCiid().equals(other.getCiid()))
            && (this.getOrderid() == null ? other.getOrderid() == null : this.getOrderid().equals(other.getOrderid()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getCidrid() == null) ? 0 : getCidrid().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getUnit() == null) ? 0 : getUnit().hashCode());
        result = prime * result + ((getMinrange() == null) ? 0 : getMinrange().hashCode());
        result = prime * result + ((getMaxrange() == null) ? 0 : getMaxrange().hashCode());
        result = prime * result + ((getNormalvalue() == null) ? 0 : getNormalvalue().hashCode());
        result = prime * result + ((getNormalvaluestring() == null) ? 0 : getNormalvaluestring().hashCode());
        result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
        result = prime * result + ((getValue() == null) ? 0 : getValue().hashCode());
        result = prime * result + ((getIserror() == null) ? 0 : getIserror().hashCode());
        result = prime * result + ((getCiid() == null) ? 0 : getCiid().hashCode());
        result = prime * result + ((getOrderid() == null) ? 0 : getOrderid().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", cidrid=").append(cidrid);
        sb.append(", name=").append(name);
        sb.append(", unit=").append(unit);
        sb.append(", minrange=").append(minrange);
        sb.append(", maxrange=").append(maxrange);
        sb.append(", normalvalue=").append(normalvalue);
        sb.append(", normalvaluestring=").append(normalvaluestring);
        sb.append(", type=").append(type);
        sb.append(", value=").append(value);
        sb.append(", iserror=").append(iserror);
        sb.append(", ciid=").append(ciid);
        sb.append(", orderid=").append(orderid);
        sb.append("]");
        return sb.toString();
    }
}