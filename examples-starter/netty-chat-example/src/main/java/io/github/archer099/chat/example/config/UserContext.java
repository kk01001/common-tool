package io.github.archer099.chat.example.config;

/**
 * @author archer099
 * @date 2026-03-07 19:00:00
 * @description 当前登录用户上下文，基于 ThreadLocal
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> NICKNAME = new ThreadLocal<>();

    public static void set(Long userId, String nickname) {
        USER_ID.set(userId);
        NICKNAME.set(nickname);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static String getNickname() {
        return NICKNAME.get();
    }

    public static void clear() {
        USER_ID.remove();
        NICKNAME.remove();
    }
}
