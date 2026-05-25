package io.github.archer099.redisson.example.controller;

import io.github.archer099.redisson.template.MultiRedissonTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.protocol.ScoredEntry;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author archer099
 * @date 2026-01-15 18:00:00
 * @description 排行榜功能 - 使用 ZSet 实现点赞排行榜
 */
@Slf4j
@RestController
@RequestMapping("/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final MultiRedissonTemplate redissonTemplate;
    private final RedissonClient redissonClient;

    /**
     * 排行榜 Key 前缀
     */
    private static final String LEADERBOARD_KEY = "leaderboard:likes";

    /**
     * 添加成员到排行榜（初始点赞数为0）
     */
    @PostMapping("/member/add")
    public Map<String, Object> addMember(@RequestParam String name) {
        // 使用 ZSet，初始分数为 0
        redissonTemplate.zadd(LEADERBOARD_KEY, name, 0);
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("memberId", name);
        result.put("initialLikes", 0);
        result.put("message", "成员已添加到排行榜");
        return result;
    }

    /**
     * 给成员点赞（增加1分）
     */
    @PostMapping("/like/{memberId}")
    public Map<String, Object> like(@PathVariable String memberId) {
        // 增加分数，返回新的分数
        Double newScore = redissonTemplate.zincrby(LEADERBOARD_KEY, memberId, 1);
        
        // 获取当前排名
        Integer rank = redissonTemplate.zrevrank(LEADERBOARD_KEY, memberId);
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("memberId", memberId);
        result.put("currentLikes", newScore != null ? newScore.intValue() : 0);
        result.put("rank", rank != null ? rank + 1 : null);  // 排名从1开始
        result.put("message", "点赞成功！");
        return result;
    }

    /**
     * 批量点赞（一次增加多个赞）
     */
    @PostMapping("/like/{memberId}/batch")
    public Map<String, Object> batchLike(@PathVariable String memberId,
                                          @RequestParam(defaultValue = "1") int count) {
        // 增加分数
        Double newScore = redissonTemplate.zincrby(LEADERBOARD_KEY, memberId, count);
        
        // 获取当前排名
        Integer rank = redissonTemplate.zrevrank(LEADERBOARD_KEY, memberId);
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("memberId", memberId);
        result.put("addedLikes", count);
        result.put("currentLikes", newScore != null ? newScore.intValue() : 0);
        result.put("rank", rank != null ? rank + 1 : null);
        return result;
    }

    /**
     * 取消点赞（减少1分）
     */
    @PostMapping("/unlike/{memberId}")
    public Map<String, Object> unlike(@PathVariable String memberId) {
        // 减少分数，但不能小于0
        Double currentScore = redissonTemplate.zscore(LEADERBOARD_KEY, memberId);
        if (currentScore == null || currentScore <= 0) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", false);
            result.put("memberId", memberId);
            result.put("message", "点赞数已为0，无法取消");
            return result;
        }
        
        Double newScore = redissonTemplate.zincrby(LEADERBOARD_KEY, memberId, -1);
        Integer rank = redissonTemplate.zrevrank(LEADERBOARD_KEY, memberId);
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("memberId", memberId);
        result.put("currentLikes", newScore != null ? newScore.intValue() : 0);
        result.put("rank", rank != null ? rank + 1 : null);
        result.put("message", "取消点赞成功");
        return result;
    }

    /**
     * 获取排行榜（Top N，按点赞数降序）
     */
    @GetMapping("/top")
    public Map<String, Object> getTopN(@RequestParam(defaultValue = "10") int limit) {
        RScoredSortedSet<String> scoredSortedSet = redissonClient.getScoredSortedSet(LEADERBOARD_KEY);
        
        // 获取 Top N（按分数降序）
        Collection<ScoredEntry<String>> entries = scoredSortedSet.entryRangeReversed(0, limit - 1);
        
        List<Map<String, Object>> rankings = new ArrayList<>();
        int rank = 1;
        for (ScoredEntry<String> entry : entries) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("rank", rank++);
            item.put("memberId", entry.getValue());
            item.put("likes", entry.getScore().intValue());
            rankings.add(item);
        }
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", scoredSortedSet.size());
        result.put("limit", limit);
        result.put("rankings", rankings);
        return result;
    }

    /**
     * 获取成员信息（排名和点赞数）
     */
    @GetMapping("/member/{memberId}")
    public Map<String, Object> getMemberInfo(@PathVariable String memberId) {
        Double score = redissonTemplate.zscore(LEADERBOARD_KEY, memberId);
        Integer rank = redissonTemplate.zrevrank(LEADERBOARD_KEY, memberId);
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("memberId", memberId);
        
        if (score != null) {
            result.put("exists", true);
            result.put("likes", score.intValue());
            result.put("rank", rank != null ? rank + 1 : null);
        } else {
            result.put("exists", false);
            result.put("message", "成员不存在");
        }
        
        return result;
    }

    /**
     * 获取指定排名范围的成员
     */
    @GetMapping("/range")
    public Map<String, Object> getRankRange(@RequestParam(defaultValue = "1") int start,
                                             @RequestParam(defaultValue = "10") int end) {
        RScoredSortedSet<String> scoredSortedSet = redissonClient.getScoredSortedSet(LEADERBOARD_KEY);
        
        // 转换为0-based索引
        int startIndex = Math.max(0, start - 1);
        int endIndex = end - 1;
        
        Collection<ScoredEntry<String>> entries = scoredSortedSet.entryRangeReversed(startIndex, endIndex);
        
        List<Map<String, Object>> rankings = new ArrayList<>();
        int rank = start;
        for (ScoredEntry<String> entry : entries) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("rank", rank++);
            item.put("memberId", entry.getValue());
            item.put("likes", entry.getScore().intValue());
            rankings.add(item);
        }
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", scoredSortedSet.size());
        result.put("start", start);
        result.put("end", end);
        result.put("rankings", rankings);
        return result;
    }

    /**
     * 移除成员
     */
    @DeleteMapping("/member/{memberId}")
    public Map<String, Object> removeMember(@PathVariable String memberId) {
        boolean removed = redissonTemplate.zrem(LEADERBOARD_KEY, memberId);
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", removed);
        result.put("memberId", memberId);
        result.put("message", removed ? "成员已移除" : "成员不存在");
        return result;
    }

    /**
     * 移除分数最低的成员（淘汰末位）
     */
    @DeleteMapping("/pop-min")
    public Map<String, Object> popMinMember() {
        // 使用 zpopMin 弹出分数最低的成员
        String removedMember = redissonTemplate.zpopMin(LEADERBOARD_KEY);
        
        Map<String, Object> result = new LinkedHashMap<>();
        if (removedMember != null) {
            result.put("success", true);
            result.put("removedMember", removedMember);
            result.put("message", "已淘汰分数最低的成员: " + removedMember);
        } else {
            result.put("success", false);
            result.put("message", "排行榜为空，无法淘汰");
        }
        return result;
    }

    /**
     * 清空排行榜
     */
    @DeleteMapping("/clear")
    public Map<String, Object> clearLeaderboard() {
        boolean deleted = redissonTemplate.delete(LEADERBOARD_KEY);
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", deleted);
        result.put("message", "排行榜已清空");
        return result;
    }

    /**
     * 获取排行榜统计信息
     */
    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        RScoredSortedSet<String> scoredSortedSet = redissonClient.getScoredSortedSet(LEADERBOARD_KEY);
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalMembers", scoredSortedSet.size());
        
        if (scoredSortedSet.size() > 0) {
            // 获取最高分
            Collection<ScoredEntry<String>> top1 = scoredSortedSet.entryRangeReversed(0, 0);
            if (!top1.isEmpty()) {
                ScoredEntry<String> topEntry = top1.iterator().next();
                result.put("topMember", topEntry.getValue());
                result.put("topLikes", topEntry.getScore().intValue());
            }
            
            // 获取最低分
            Collection<ScoredEntry<String>> bottom1 = scoredSortedSet.entryRange(0, 0);
            if (!bottom1.isEmpty()) {
                ScoredEntry<String> bottomEntry = bottom1.iterator().next();
                result.put("bottomMember", bottomEntry.getValue());
                result.put("bottomLikes", bottomEntry.getScore().intValue());
            }
        }
        
        return result;
    }

    /**
     * 初始化测试数据
     */
    @PostMapping("/init-demo")
    public Map<String, Object> initDemoData() {
        // 清空现有数据
        redissonTemplate.delete(LEADERBOARD_KEY);
        
        // 添加测试成员
        String[] members = {"张三", "李四", "王五", "赵六", "钱七", "孙八", "周九", "吴十"};
        int[] likes = {128, 256, 64, 512, 32, 96, 1024, 48};
        
        for (int i = 0; i < members.length; i++) {
            redissonTemplate.zadd(LEADERBOARD_KEY, members[i], likes[i]);
        }
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("message", "已初始化 " + members.length + " 个测试成员");
        result.put("members", members);
        return result;
    }
}
