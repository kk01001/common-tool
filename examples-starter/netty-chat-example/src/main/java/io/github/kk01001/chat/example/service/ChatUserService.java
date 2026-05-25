package io.github.archer099.chat.example.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.archer099.chat.example.entity.ChatUser;
import io.github.archer099.chat.example.mapper.ChatUserMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author archer099
 * @date 2026-03-07 18:00:00
 * @description 用户服务
 */
@Service
public class ChatUserService extends ServiceImpl<ChatUserMapper, ChatUser> {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public ChatUser findByUsername(String username) {
        return getOne(new LambdaQueryWrapper<ChatUser>()
                .eq(ChatUser::getUsername, username));
    }

    public ChatUser login(String username, String password) {
        ChatUser user = findByUsername(username);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return null;
        }
        user.setLastLoginTime(LocalDateTime.now());
        updateById(user);
        return user;
    }

    public ChatUser register(String username, String password, String nickname) {
        if (findByUsername(username) != null) {
            return null;
        }
        ChatUser user = new ChatUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        user.setStatus(1);
        save(user);
        return user;
    }

    public List<ChatUser> searchUsers(String keyword) {
        return list(new LambdaQueryWrapper<ChatUser>()
                .like(ChatUser::getUsername, keyword)
                .or()
                .like(ChatUser::getNickname, keyword)
                .last("LIMIT 20"));
    }
}
