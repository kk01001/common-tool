package io.github.kk01001.chat.example.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.kk01001.chat.example.entity.ChatConversation;
import io.github.kk01001.chat.example.entity.ChatMessage;
import io.github.kk01001.chat.example.mapper.ChatConversationMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author kk01001
 * @date 2026-03-07 18:00:00
 * @description 会话服务
 */
@Service
public class ChatConversationService extends ServiceImpl<ChatConversationMapper, ChatConversation> {

    /**
     * 获取用户的会话列表
     */
    public List<ChatConversation> getUserConversations(Long userId) {
        return list(new LambdaQueryWrapper<ChatConversation>()
                .eq(ChatConversation::getUserId, userId)
                .eq(ChatConversation::getStatus, 1)
                .orderByDesc(ChatConversation::getLastMsgTime));
    }

    /**
     * 更新或创建会话
     */
    public void updateConversation(Long userId, Long targetId, String chatType, ChatMessage message, boolean incrementUnread) {
        ChatConversation conv = getOne(new LambdaQueryWrapper<ChatConversation>()
                .eq(ChatConversation::getUserId, userId)
                .eq(ChatConversation::getTargetId, targetId)
                .eq(ChatConversation::getChatType, chatType));

        String summary = buildSummary(message);

        if (conv == null) {
            conv = new ChatConversation();
            conv.setUserId(userId);
            conv.setTargetId(targetId);
            conv.setChatType(chatType);
            conv.setLastMsgId(message.getId());
            conv.setLastMsgContent(summary);
            conv.setLastMsgTime(LocalDateTime.now());
            conv.setUnreadCount(incrementUnread ? 1 : 0);
            conv.setStatus(1);
            save(conv);
        } else {
            conv.setLastMsgId(message.getId());
            conv.setLastMsgContent(summary);
            conv.setLastMsgTime(LocalDateTime.now());
            conv.setStatus(1);
            if (incrementUnread) {
                conv.setUnreadCount(conv.getUnreadCount() + 1);
            }
            updateById(conv);
        }
    }

    /**
     * 清除未读
     */
    public void clearUnread(Long userId, Long targetId, String chatType) {
        update(new LambdaUpdateWrapper<ChatConversation>()
                .eq(ChatConversation::getUserId, userId)
                .eq(ChatConversation::getTargetId, targetId)
                .eq(ChatConversation::getChatType, chatType)
                .set(ChatConversation::getUnreadCount, 0));
    }

    /**
     * 删除会话（软删除）
     */
    public void deleteConversation(Long convId, Long userId) {
        update(new LambdaUpdateWrapper<ChatConversation>()
                .eq(ChatConversation::getId, convId)
                .eq(ChatConversation::getUserId, userId)
                .set(ChatConversation::getStatus, 0));
    }

    private String buildSummary(ChatMessage message) {
        return switch (message.getMsgType()) {
            case "IMAGE" -> "[图片]";
            case "FILE" -> "[文件]";
            case "AUDIO" -> "[语音]";
            case "SYSTEM" -> "[系统消息]";
            default -> {
                String content = message.getContent();
                yield content != null && content.length() > 50 ? content.substring(0, 50) + "..." : content;
            }
        };
    }
}
