package com.it.xxhealth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import lombok.Data;

/**
 * 用户预约订单实体，对应 orders 表。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
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
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
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

    /**
     * 医生端订单列表和详情需要展示的关联信息。
     * 这些字段由 OrdersMapper 的联表查询填充，不参与 orders 表写入。
     */
    @TableField(exist = false)
    private Users users;

    /**
     * 订单关联的体检机构信息。
     */
    @TableField(exist = false)
    private Hospital hospital;

    /**
     * 订单关联的体检套餐信息。
     */
    @TableField(exist = false)
    private Setmeal setmeal;

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
        Orders other = (Orders) that;
        return (this.getOrderid() == null ? other.getOrderid() == null : this.getOrderid().equals(other.getOrderid()))
            && (this.getOrderdate() == null ? other.getOrderdate() == null : this.getOrderdate().equals(other.getOrderdate()))
            && (this.getUserid() == null ? other.getUserid() == null : this.getUserid().equals(other.getUserid()))
            && (this.getHpid() == null ? other.getHpid() == null : this.getHpid().equals(other.getHpid()))
            && (this.getSmid() == null ? other.getSmid() == null : this.getSmid().equals(other.getSmid()))
            && (this.getState() == null ? other.getState() == null : this.getState().equals(other.getState()));
    }

    /**
     * 根据业务字段计算对象哈希值。
     * @return 当前对象的哈希值
     */
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
