package io.github.kk01001.chat.example.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.kk01001.chat.example.entity.ChatFriendship;
import io.github.kk01001.chat.example.mapper.ChatFriendshipMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author kk01001
 * @date 2026-03-07 18:00:00
 * @description 好友关系服务
 */
@Service
public class ChatFriendshipService extends ServiceImpl<ChatFriendshipMapper, ChatFriendship> {

    public ChatFriendship sendRequest(Long userId, Long friendId, String requestMsg) {
        ChatFriendship existing = getOne(new LambdaQueryWrapper<ChatFriendship>()
                .eq(ChatFriendship::getUserId, userId)
                .eq(ChatFriendship::getFriendId, friendId));
        if (existing != null) {
            return existing;
        }

        ChatFriendship friendship = new ChatFriendship();
        friendship.setUserId(userId);
        friendship.setFriendId(friendId);
        friendship.setStatus(0);
        friendship.setRequestMsg(requestMsg);
        save(friendship);
        return friendship;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean acceptRequest(Long requestId, Long currentUserId) {
        ChatFriendship request = getById(requestId);
        if (request == null || !request.getFriendId().equals(currentUserId) || request.getStatus() != 0) {
            return false;
        }

        request.setStatus(1);
        request.setUpdateTime(LocalDateTime.now());
        updateById(request);

        ChatFriendship reverse = getOne(new LambdaQueryWrapper<ChatFriendship>()
                .eq(ChatFriendship::getUserId, currentUserId)
                .eq(ChatFriendship::getFriendId, request.getUserId()));
        if (reverse == null) {
            reverse = new ChatFriendship();
            reverse.setUserId(currentUserId);
            reverse.setFriendId(request.getUserId());
            reverse.setStatus(1);
            save(reverse);
        } else {
            reverse.setStatus(1);
            reverse.setUpdateTime(LocalDateTime.now());
            updateById(reverse);
        }
        return true;
    }

    public boolean rejectRequest(Long requestId, Long currentUserId) {
        ChatFriendship request = getById(requestId);
        if (request == null || !request.getFriendId().equals(currentUserId) || request.getStatus() != 0) {
            return false;
        }
        request.setStatus(2);
        request.setUpdateTime(LocalDateTime.now());
        return updateById(request);
    }

    public List<ChatFriendship> getPendingRequests(Long userId) {
        return list(new LambdaQueryWrapper<ChatFriendship>()
                .eq(ChatFriendship::getFriendId, userId)
                .eq(ChatFriendship::getStatus, 0)
                .orderByDesc(ChatFriendship::getCreateTime));
    }

    public List<ChatFriendship> getFriendList(Long userId) {
        return list(new LambdaQueryWrapper<ChatFriendship>()
                .eq(ChatFriendship::getUserId, userId)
                .eq(ChatFriendship::getStatus, 1));
    }

    public boolean isFriend(Long userId, Long friendId) {
        return count(new LambdaQueryWrapper<ChatFriendship>()
                .eq(ChatFriendship::getUserId, userId)
                .eq(ChatFriendship::getFriendId, friendId)
                .eq(ChatFriendship::getStatus, 1)) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFriend(Long userId, Long friendId) {
        remove(new LambdaQueryWrapper<ChatFriendship>()
                .eq(ChatFriendship::getUserId, userId)
                .eq(ChatFriendship::getFriendId, friendId));
        remove(new LambdaQueryWrapper<ChatFriendship>()
                .eq(ChatFriendship::getUserId, friendId)
                .eq(ChatFriendship::getFriendId, userId));
        return true;
    }
}
