package com.it.xxhealth.service;

import com.it.xxhealth.dto.MessageQueryDto;
import com.it.xxhealth.entity.Message;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 消息通知 Service
 */
public interface MessageService extends IService<Message> {

    /**
     * 查询消息列表（按类型 / 关键字 / 状态过滤），按时间倒序
     */
    List<Message> listMessages(MessageQueryDto query);

    /**
     * 单条标记已读
     *
     * @return 影响行数
     */
    int markRead(Integer msgid);

    /**
     * 全部标记已读
     *
     * @return 影响行数
     */
    int markAllRead();

    /**
     * 未读消息数
     */
    int unreadCount();
}
