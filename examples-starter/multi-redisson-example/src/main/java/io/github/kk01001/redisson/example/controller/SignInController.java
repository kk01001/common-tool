package io.github.archer099.redisson.example.controller;

import io.github.archer099.redisson.template.MultiRedissonTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author archer099
 * @date 2026-01-17 19:30:00
 * @description 签到系统 Demo - 使用 BitSet 实现用户签到
 * <p>
 * 应用场景：
 * 1. 用户每日签到
 * 2. 连续签到统计
 * 3. 签到日历展示
 * 4. 签到奖励发放
 * </p>
 * <p>
 * 存储结构：
 * - Key: sign:用户ID:年月 (如 sign:user001:2026-01)
 * - Bit位置：当月第几天（1-31）
 * - Bit值：1表示已签到，0表示未签到
 * </p>
 */
@RestController
@RequestMapping("/api/signin")
public class SignInController {

    private static final String SIGN_KEY_PREFIX = "demo:sign:";
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final MultiRedissonTemplate redissonTemplate;

    public SignInController(MultiRedissonTemplate redissonTemplate) {
        this.redissonTemplate = redissonTemplate;
    }

    /**
     * 用户签到
     *
     * @param userId 用户ID
     * @param date   签到日期（可选，默认今天）
     */
    @PostMapping("/checkin")
    public Map<String, Object> signIn(
            @RequestParam(defaultValue = "user001") String userId,
            @RequestParam(required = false) String date) {

        Map<String, Object> result = new LinkedHashMap<>();

        LocalDate signDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        String key = buildKey(userId, signDate);
        int dayOfMonth = signDate.getDayOfMonth();

        // 检查是否已签到
        Boolean alreadySigned = redissonTemplate.getBit(key, dayOfMonth);
        if (Boolean.TRUE.equals(alreadySigned)) {
            result.put("success", false);
            result.put("message", "今日已签到，请勿重复签到");
            result.put("userId", userId);
            result.put("date", signDate.toString());
            return result;
        }

        // 执行签到
        redissonTemplate.setBit(key, dayOfMonth, true);

        // 计算本月签到天数
        long monthSignCount = redissonTemplate.bitCount(key);

        // 计算连续签到天数
        int consecutiveDays = getConsecutiveDays(userId, signDate);

        result.put("success", true);
        result.put("message", "签到成功！");
        result.put("userId", userId);
        result.put("date", signDate.toString());
        result.put("monthSignCount", monthSignCount);
        result.put("consecutiveDays", consecutiveDays);

        // 签到奖励
        int reward = calculateReward(consecutiveDays);
        result.put("reward", reward);
        result.put("rewardMessage", getRewardMessage(consecutiveDays, reward));

        return result;
    }

    /**
     * 查询某天是否签到
     */
    @GetMapping("/check")
    public Map<String, Object> checkSignIn(
            @RequestParam(defaultValue = "user001") String userId,
            @RequestParam(required = false) String date) {

        Map<String, Object> result = new LinkedHashMap<>();

        LocalDate checkDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        String key = buildKey(userId, checkDate);
        int dayOfMonth = checkDate.getDayOfMonth();

        Boolean signed = redissonTemplate.getBit(key, dayOfMonth);

        result.put("userId", userId);
        result.put("date", checkDate.toString());
        result.put("signed", Boolean.TRUE.equals(signed));
        result.put("message", Boolean.TRUE.equals(signed) ? "已签到" : "未签到");

        return result;
    }

    /**
     * 获取月度签到统计
     */
    @GetMapping("/monthly")
    public Map<String, Object> getMonthlyStats(
            @RequestParam(defaultValue = "user001") String userId,
            @RequestParam(required = false) String month) {

        Map<String, Object> result = new LinkedHashMap<>();

        YearMonth yearMonth = month != null ?
                YearMonth.parse(month) : YearMonth.now();
        String key = SIGN_KEY_PREFIX + userId + ":" + yearMonth.format(MONTH_FORMATTER);

        // 本月总天数
        int daysInMonth = yearMonth.lengthOfMonth();

        // 已签到天数
        long signedDays = redissonTemplate.bitCount(key);

        // 签到日期列表
        List<Integer> signedDateList = new ArrayList<>();
        for (int day = 1; day <= daysInMonth; day++) {
            Boolean signed = redissonTemplate.getBit(key, day);
            if (Boolean.TRUE.equals(signed)) {
                signedDateList.add(day);
            }
        }

        // 签到日历
        List<Map<String, Object>> calendar = new ArrayList<>();
        for (int day = 1; day <= daysInMonth; day++) {
            Map<String, Object> dayInfo = new LinkedHashMap<>();
            dayInfo.put("day", day);
            Boolean signed = redissonTemplate.getBit(key, day);
            dayInfo.put("signed", Boolean.TRUE.equals(signed));
            dayInfo.put("date", yearMonth.atDay(day).toString());
            calendar.add(dayInfo);
        }

        result.put("userId", userId);
        result.put("month", yearMonth.toString());
        result.put("daysInMonth", daysInMonth);
        result.put("signedDays", signedDays);
        result.put("signRate", String.format("%.1f%%", (double) signedDays / daysInMonth * 100));
        result.put("signedDateList", signedDateList);
        result.put("calendar", calendar);

        return result;
    }

    /**
     * 获取连续签到天数
     */
    @GetMapping("/consecutive")
    public Map<String, Object> getConsecutiveDays(
            @RequestParam(defaultValue = "user001") String userId) {

        Map<String, Object> result = new LinkedHashMap<>();

        LocalDate today = LocalDate.now();
        int consecutiveDays = getConsecutiveDays(userId, today);

        result.put("userId", userId);
        result.put("consecutiveDays", consecutiveDays);
        result.put("message", getConsecutiveMessage(consecutiveDays));

        // 下一个奖励里程碑
        int[] milestones = {3, 7, 14, 21, 30};
        for (int milestone : milestones) {
            if (consecutiveDays < milestone) {
                result.put("nextMilestone", milestone);
                result.put("daysToMilestone", milestone - consecutiveDays);
                break;
            }
        }

        return result;
    }

    /**
     * 补签（需要消耗道具/积分）
     */
    @PostMapping("/retroactive")
    public Map<String, Object> retroactiveSignIn(
            @RequestParam(defaultValue = "user001") String userId,
            @RequestParam String date) {

        Map<String, Object> result = new LinkedHashMap<>();

        LocalDate signDate = LocalDate.parse(date);
        LocalDate today = LocalDate.now();

        // 检查日期有效性
        if (signDate.isAfter(today)) {
            result.put("success", false);
            result.put("message", "不能补签未来的日期");
            return result;
        }

        if (signDate.isBefore(today.minusDays(7))) {
            result.put("success", false);
            result.put("message", "只能补签7天内的日期");
            return result;
        }

        String key = buildKey(userId, signDate);
        int dayOfMonth = signDate.getDayOfMonth();

        // 检查是否已签到
        Boolean signed = redissonTemplate.getBit(key, dayOfMonth);
        if (Boolean.TRUE.equals(signed)) {
            result.put("success", false);
            result.put("message", "该日期已签到，无需补签");
            return result;
        }

        // 执行补签
        redissonTemplate.setBit(key, dayOfMonth, true);

        result.put("success", true);
        result.put("message", "补签成功！消耗补签卡1张");
        result.put("userId", userId);
        result.put("date", signDate.toString());
        result.put("cost", "补签卡 x1");

        return result;
    }

    /**
     * 获取签到统计排行榜（本月签到天数）
     */
    @GetMapping("/ranking")
    public Map<String, Object> getSignInRanking(
            @RequestParam(defaultValue = "10") int limit) {

        Map<String, Object> result = new LinkedHashMap<>();

        // 这里简单演示，实际应从用户列表中获取
        String[] demoUsers = {"user001", "user002", "user003", "user004", "user005"};
        YearMonth currentMonth = YearMonth.now();

        List<Map<String, Object>> ranking = new ArrayList<>();
        for (String uId : demoUsers) {
            String key = SIGN_KEY_PREFIX + uId + ":" + currentMonth.format(MONTH_FORMATTER);
            long signedDays = redissonTemplate.bitCount(key);

            Map<String, Object> userRank = new LinkedHashMap<>();
            userRank.put("userId", uId);
            userRank.put("signedDays", signedDays);
            ranking.add(userRank);
        }

        // 按签到天数排序
        ranking.sort((a, b) -> Long.compare((Long) b.get("signedDays"), (Long) a.get("signedDays")));

        // 添加排名
        for (int i = 0; i < ranking.size(); i++) {
            ranking.get(i).put("rank", i + 1);
        }

        result.put("month", currentMonth.toString());
        result.put("ranking", ranking.subList(0, Math.min(limit, ranking.size())));

        return result;
    }

    /**
     * 重置签到数据（测试用）
     */
    @PostMapping("/reset")
    public Map<String, Object> resetSignIn(
            @RequestParam(defaultValue = "user001") String userId,
            @RequestParam(required = false) String month) {

        Map<String, Object> result = new LinkedHashMap<>();

        YearMonth yearMonth = month != null ?
                YearMonth.parse(month) : YearMonth.now();
        String key = SIGN_KEY_PREFIX + userId + ":" + yearMonth.format(MONTH_FORMATTER);

        redissonTemplate.delete(key);

        result.put("success", true);
        result.put("userId", userId);
        result.put("month", yearMonth.toString());
        result.put("message", "签到数据已重置");

        return result;
    }

    // ==================== 私有方法 ====================

    /**
     * 构建 Redis Key
     */
    private String buildKey(String userId, LocalDate date) {
        YearMonth yearMonth = YearMonth.from(date);
        return SIGN_KEY_PREFIX + userId + ":" + yearMonth.format(MONTH_FORMATTER);
    }

    /**
     * 计算连续签到天数
     */
    private int getConsecutiveDays(String userId, LocalDate endDate) {
        int count = 0;
        LocalDate checkDate = endDate;

        while (true) {
            String key = buildKey(userId, checkDate);
            Boolean signed = redissonTemplate.getBit(key, checkDate.getDayOfMonth());

            if (Boolean.TRUE.equals(signed)) {
                count++;
                checkDate = checkDate.minusDays(1);
            } else {
                break;
            }

            // 最多检查365天
            if (count >= 365) {
                break;
            }
        }

        return count;
    }

    /**
     * 计算签到奖励
     */
    private int calculateReward(int consecutiveDays) {
        if (consecutiveDays >= 30) {
            return 100;
        } else if (consecutiveDays >= 21) {
            return 50;
        } else if (consecutiveDays >= 14) {
            return 30;
        } else if (consecutiveDays >= 7) {
            return 20;
        } else if (consecutiveDays >= 3) {
            return 10;
        } else {
            return 5;
        }
    }

    /**
     * 获取奖励消息
     */
    private String getRewardMessage(int consecutiveDays, int reward) {
        if (consecutiveDays >= 30) {
            return String.format("连续签到%d天，达成月度全勤！奖励积分 +%d", consecutiveDays, reward);
        } else if (consecutiveDays >= 7) {
            return String.format("连续签到%d天，奖励积分 +%d", consecutiveDays, reward);
        } else if (consecutiveDays >= 3) {
            return String.format("连续签到%d天，奖励积分 +%d，继续加油！", consecutiveDays, reward);
        } else {
            return String.format("签到成功，奖励积分 +%d", reward);
        }
    }

    /**
     * 获取连续签到消息
     */
    private String getConsecutiveMessage(int consecutiveDays) {
        if (consecutiveDays == 0) {
            return "今日还未签到";
        } else if (consecutiveDays >= 30) {
            return String.format("太厉害了！已连续签到 %d 天", consecutiveDays);
        } else if (consecutiveDays >= 7) {
            return String.format("很棒！已连续签到 %d 天", consecutiveDays);
        } else {
            return String.format("已连续签到 %d 天，继续保持！", consecutiveDays);
        }
    }
}
