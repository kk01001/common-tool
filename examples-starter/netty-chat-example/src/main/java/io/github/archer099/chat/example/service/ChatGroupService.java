package io.github.archer099.chat.example.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.archer099.chat.example.entity.ChatGroup;
import io.github.archer099.chat.example.entity.ChatGroupMember;
import io.github.archer099.chat.example.mapper.ChatGroupMapper;
import io.github.archer099.chat.example.mapper.ChatGroupMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author archer099
 * @date 2026-03-07 18:00:00
 * @description 群组服务
 */
@Service
@RequiredArgsConstructor
public class ChatGroupService extends ServiceImpl<ChatGroupMapper, ChatGroup> {

    private final ChatGroupMemberMapper groupMemberMapper;

    @Transactional(rollbackFor = Exception.class)
    public ChatGroup createGroup(String name, Long ownerId) {
        ChatGroup group = new ChatGroup();
        group.setName(name);
        group.setOwnerId(ownerId);
        group.setMaxMembers(200);
        group.setStatus(1);
        save(group);

        ChatGroupMember owner = new ChatGroupMember();
        owner.setGroupId(group.getId());
        owner.setUserId(ownerId);
        owner.setRole(2);
        groupMemberMapper.insert(owner);

        return group;
    }

    public boolean addMember(Long groupId, Long userId) {
        Long count = groupMemberMapper.selectCount(new LambdaQueryWrapper<ChatGroupMember>()
                .eq(ChatGroupMember::getGroupId, groupId)
                .eq(ChatGroupMember::getUserId, userId));
        if (count > 0) {
            return false;
        }

        ChatGroupMember member = new ChatGroupMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setRole(0);
        groupMemberMapper.insert(member);
        return true;
    }

    public boolean removeMember(Long groupId, Long userId) {
        return groupMemberMapper.delete(new LambdaQueryWrapper<ChatGroupMember>()
                .eq(ChatGroupMember::getGroupId, groupId)
                .eq(ChatGroupMember::getUserId, userId)) > 0;
    }

    public List<ChatGroupMember> getMembers(Long groupId) {
        return groupMemberMapper.selectList(new LambdaQueryWrapper<ChatGroupMember>()
                .eq(ChatGroupMember::getGroupId, groupId));
    }

    public List<Long> getMemberUserIds(Long groupId) {
        return getMembers(groupId).stream()
                .map(ChatGroupMember::getUserId)
                .toList();
    }

    public boolean isMember(Long groupId, Long userId) {
        return groupMemberMapper.selectCount(new LambdaQueryWrapper<ChatGroupMember>()
                .eq(ChatGroupMember::getGroupId, groupId)
                .eq(ChatGroupMember::getUserId, userId)) > 0;
    }

    public List<ChatGroup> getUserGroups(Long userId) {
        List<Long> groupIds = groupMemberMapper.selectList(
                        new LambdaQueryWrapper<ChatGroupMember>()
                                .eq(ChatGroupMember::getUserId, userId))
                .stream()
                .map(ChatGroupMember::getGroupId)
                .toList();
        if (groupIds.isEmpty()) {
            return List.of();
        }
        return list(new LambdaQueryWrapper<ChatGroup>()
                .in(ChatGroup::getId, groupIds)
                .eq(ChatGroup::getStatus, 1));
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean dissolveGroup(Long groupId, Long ownerId) {
        ChatGroup group = getById(groupId);
        if (group == null || !group.getOwnerId().equals(ownerId)) {
            return false;
        }
        group.setStatus(0);
        updateById(group);
        groupMemberMapper.delete(new LambdaQueryWrapper<ChatGroupMember>()
                .eq(ChatGroupMember::getGroupId, groupId));
        return true;
    }
}
