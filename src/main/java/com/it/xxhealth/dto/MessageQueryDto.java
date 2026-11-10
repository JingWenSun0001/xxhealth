package com.it.xxhealth.dto;

import lombok.Data;

/**
 * 消息查询参数
 */
@Data
public class MessageQueryDto {

    /** 消息类型，all 或空表示全部 */
    private String type;

    /** 关键字（标题 / 内容） */
    private String keyword;

    /** 状态：all / unread / read */
    private String status;
}
