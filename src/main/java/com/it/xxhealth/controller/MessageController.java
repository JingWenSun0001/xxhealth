package com.it.xxhealth.controller;

import com.it.xxhealth.common.Result;
import com.it.xxhealth.dto.MessageQueryDto;
import com.it.xxhealth.entity.Message;
import com.it.xxhealth.service.MessageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 医生端：消息通知
 */
@RestController
@RequestMapping("/message")
public class MessageController {

    @Resource
    private MessageService messageService;

    /**
     * 消息列表（支持类型 / 关键字 / 已读状态过滤）
     */
    @PostMapping("/list")
    public Result<List<Message>> list(@RequestBody(required = false) MessageQueryDto query) {
        return Result.success(messageService.listMessages(query));
    }

    /**
     * 单条标记已读
     */
    @PostMapping("/markRead")
    public Result<Integer> markRead(@RequestBody Message message) {
        if (message == null || message.getMsgid() == null) {
            return Result.error(400, "消息编号不能为空");
        }
        return Result.success(messageService.markRead(message.getMsgid()));
    }

    /**
     * 全部标记已读
     */
    @PostMapping("/markAllRead")
    public Result<Integer> markAllRead() {
        return Result.success(messageService.markAllRead());
    }

    /**
     * 未读消息数
     */
    @PostMapping("/unreadCount")
    public Result<Integer> unreadCount() {
        return Result.success(messageService.unreadCount());
    }
}
