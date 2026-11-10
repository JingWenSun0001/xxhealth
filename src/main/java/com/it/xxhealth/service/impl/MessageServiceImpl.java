package com.it.xxhealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.xxhealth.dto.MessageQueryDto;
import com.it.xxhealth.entity.Message;
import com.it.xxhealth.mapper.MessageMapper;
import com.it.xxhealth.service.MessageService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message>
        implements MessageService {

    @Override
    public List<Message> listMessages(MessageQueryDto query) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            String type = query.getType();
            if (type != null && !type.trim().isEmpty() && !"all".equalsIgnoreCase(type)) {
                wrapper.eq(Message::getType, type.trim());
            }
            String keyword = query.getKeyword();
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = keyword.trim();
                wrapper.and(w -> w.like(Message::getTitle, kw).or().like(Message::getContent, kw));
            }
            String status = query.getStatus();
            if ("unread".equalsIgnoreCase(status)) {
                wrapper.eq(Message::getIsread, 0);
            } else if ("read".equalsIgnoreCase(status)) {
                wrapper.eq(Message::getIsread, 1);
            }
        }
        wrapper.orderByDesc(Message::getCreatetime).orderByDesc(Message::getMsgid);
        return this.baseMapper.selectList(wrapper);
    }

    @Override
    public int markRead(Integer msgid) {
        if (msgid == null) {
            return 0;
        }
        Message update = new Message();
        update.setMsgid(msgid);
        update.setIsread(1);
        return this.baseMapper.updateById(update);
    }

    @Override
    public int markAllRead() {
        LambdaUpdateWrapper<Message> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Message::getIsread, 0).set(Message::getIsread, 1);
        return this.baseMapper.update(null, wrapper);
    }

    @Override
    public int unreadCount() {
        Long count = this.baseMapper.selectCount(new LambdaQueryWrapper<Message>().eq(Message::getIsread, 0));
        return count == null ? 0 : count.intValue();
    }
}
