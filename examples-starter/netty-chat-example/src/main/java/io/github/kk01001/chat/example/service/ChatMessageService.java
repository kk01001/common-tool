package io.github.kk01001.chat.example.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.kk01001.chat.example.entity.ChatMessage;
import io.github.kk01001.chat.example.mapper.ChatMessageMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author kk01001
 * @date 2026-03-07 18:00:00
 * @description 消息服务
 */
@Service
public class ChatMessageService extends ServiceImpl<ChatMessageMapper, ChatMessage> {

    /**
     * 获取单聊历史消息
     */
    public List<ChatMessage> getPrivateHistory(Long userId, Long targetId, Long beforeId, int limit) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getChatType, "PRIVATE")
                .and(w -> w
                        .and(w1 -> w1.eq(ChatMessage::getSenderId, userId).eq(ChatMessage::getReceiverId, targetId))
                        .or(w2 -> w2.eq(ChatMessage::getSenderId, targetId).eq(ChatMessage::getReceiverId, userId)))
                .ne(ChatMessage::getStatus, 2)
                .orderByDesc(ChatMessage::getId)
                .last("LIMIT " + limit);
        if (beforeId != null && beforeId > 0) {
            wrapper.lt(ChatMessage::getId, beforeId);
        }
        return list(wrapper);
    }

    /**
     * 获取群聊历史消息
     */
    public List<ChatMessage> getGroupHistory(Long groupId, Long beforeId, int limit) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getChatType, "GROUP")
                .eq(ChatMessage::getGroupId, groupId)
                .ne(ChatMessage::getStatus, 2)
                .orderByDesc(ChatMessage::getId)
                .last("LIMIT " + limit);
        if (beforeId != null && beforeId > 0) {
            wrapper.lt(ChatMessage::getId, beforeId);
        }
        return list(wrapper);
    }

    /**
     * 获取离线消息（用户不在线期间收到的未读消息）
     */
    public List<ChatMessage> getOfflineMessages(Long userId) {
        return list(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getReceiverId, userId)
                .eq(ChatMessage::getChatType, "PRIVATE")
                .eq(ChatMessage::getStatus, 0)
                .orderByAsc(ChatMessage::getCreateTime)
                .last("LIMIT 500"));
    }

    /**
     * 标记消息已读
     */
    public void markAsRead(Long userId, Long senderId) {
        update(new LambdaUpdateWrapper<ChatMessage>()
                .eq(ChatMessage::getReceiverId, userId)
                .eq(ChatMessage::getSenderId, senderId)
                .eq(ChatMessage::getChatType, "PRIVATE")
                .eq(ChatMessage::getStatus, 0)
                .set(ChatMessage::getStatus, 1));
    }
}
