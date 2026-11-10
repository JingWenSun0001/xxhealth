package com.it.xxhealth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 消息通知
 * @TableName message
 */
@TableName(value = "message")
@Data
public class Message {

    /** 消息编号 */
    @TableId(type = IdType.AUTO)
    private Integer msgid;

    /** 消息类型：预约提醒 / 报告通知 / 异常提醒 / 系统通知 / 其他消息 */
    private String type;

    /** 消息标题 */
    private String title;

    /** 消息内容 */
    private String content;

    /** 是否已读 0 未读 / 1 已读 */
    private Integer isread;

    /** 创建时间 */
    private Date createtime;
}
