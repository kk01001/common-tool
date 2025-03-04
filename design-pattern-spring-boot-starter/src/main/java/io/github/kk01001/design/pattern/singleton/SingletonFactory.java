package io.github.kk01001.design.pattern.singleton;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 单例工厂，用于创建和管理单例对象
 */
@Slf4j
public class SingletonFactory {

    /**
     * 单例对象容器
     */
    private static final Map<Class<?>, Object> SINGLETON_MAP = new ConcurrentHashMap<>();

    /**
     * 可重入锁，用于确保线程安全
     */
    private static final ReentrantLock LOCK = new ReentrantLock();

    /**
     * 私有构造方法，防止实例化
     */
    private SingletonFactory() {
        throw new UnsupportedOperationException("单例工厂不允许实例化");
    }

    /**
     * 获取单例对象
     *
     * @param clazz 类型
     * @param <T>   泛型
     * @return 单例对象
     */
    public static <T> T getInstance(Class<T> clazz) {
        // 检查类是否标记了单例模式注解
        SingletonPattern annotation = clazz.getAnnotation(SingletonPattern.class);
        if (annotation == null) {
            throw new IllegalArgumentException("类" + clazz.getName() + "未标记@SingletonPattern注解");
        }

        // 根据单例模式类型创建单例对象
        SingletonPattern.Type type = annotation.type();
        return switch (type) {
            case EAGER -> getEagerSingleton(clazz);
            case LAZY -> getLazySingleton(clazz);
            case DOUBLE_CHECK -> getDoubleCheckSingleton(clazz);
            case ENUM -> getEnumSingleton(clazz);
            default -> throw new IllegalArgumentException("不支持的单例模式类型：" + type);
        };
    }

    /**
     * 获取枚举单例对象
     *
     * @param clazz 类型
     * @param <T>   泛型
     * @return 单例对象
     */
    @SuppressWarnings("unchecked")
    private static <T> T getEnumSingleton(Class<T> clazz) {
        // 检查是否是枚举类型
        if (!clazz.isEnum()) {
            throw new IllegalArgumentException("类" + clazz.getName() + "不是枚举类型，无法使用ENUM单例模式");
        }
        
        // 枚举类型的单例，获取第一个枚举常量
        Object instance = SINGLETON_MAP.get(clazz);
        if (instance == null) {
            try {
                // 获取枚举的所有常量
                T[] enumConstants = clazz.getEnumConstants();
                if (enumConstants == null || enumConstants.length == 0) {
                    throw new IllegalArgumentException("枚举类" + clazz.getName() + "没有定义任何常量");
                }
                // 使用第一个枚举常量作为单例
                instance = enumConstants[0];
                SINGLETON_MAP.put(clazz, instance);
            } catch (Exception e) {
                log.error("获取枚举单例对象失败：{}", clazz.getName(), e);
                throw new RuntimeException("获取枚举单例对象失败：" + clazz.getName(), e);
            }
        }
        return (T) instance;
    }
    
    /**
     * 获取懒加载单例对象
     *
     * @param clazz 类型
     * @param <T>   泛型
     * @return 单例对象
     */
    @SuppressWarnings("unchecked")
    private static <T> T getLazySingleton(Class<T> clazz) {
        // 懒加载单例，先检查容器中是否存在
        Object instance = SINGLETON_MAP.get(clazz);
        if (instance == null) {
            // 不存在则创建并放入容器
            LOCK.lock();
            try {
                instance = SINGLETON_MAP.get(clazz);
                if (instance == null) {
                    instance = createInstance(clazz);
                    SINGLETON_MAP.put(clazz, instance);
                }
            } finally {
                LOCK.unlock();
            }
        }
        return (T) instance;
    }

    /**
     * 获取双重检查单例对象
     *
     * @param clazz 类型
     * @param <T>   泛型
     * @return 单例对象
     */
    @SuppressWarnings("unchecked")
    private static <T> T getDoubleCheckSingleton(Class<T> clazz) {
        // 双重检查单例，先检查容器中是否存在
        Object instance = SINGLETON_MAP.get(clazz);
        if (instance == null) {
            // 不存在则创建并放入容器，使用双重检查锁定模式
            LOCK.lock();
            try {
                instance = SINGLETON_MAP.get(clazz);
                if (instance == null) {
                    instance = createInstance(clazz);
                    SINGLETON_MAP.put(clazz, instance);
                }
            } finally {
                LOCK.unlock();
            }
        }
        return (T) instance;
    }

    /**
     * 获取饿汉式单例对象
     *
     * @param clazz 类型
     * @param <T>   泛型
     * @return 单例对象
     */
    @SuppressWarnings("unchecked")
    private static <T> T getEagerSingleton(Class<T> clazz) {
        // 饿汉式单例直接从容器中获取，如果不存在则创建
        return (T) SINGLETON_MAP.computeIfAbsent(clazz, k -> createInstance(clazz));
    }

    /**
     * 创建实例
     *
     * @param clazz 类型
     * @param <T>   泛型
     * @return 实例对象
     */
    private static <T> T createInstance(Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            log.error("创建单例对象失败：{}", clazz.getName(), e);
            throw new RuntimeException("创建单例对象失败：" + clazz.getName(), e);
        }
    }

    /**
     * 注册单例对象
     *
     * @param clazz    类型
     * @param instance 实例
     * @param <T>      泛型
     */
    public static <T> void register(Class<T> clazz, T instance) {
        if (instance == null) {
            throw new IllegalArgumentException("单例对象不能为空");
        }
        SINGLETON_MAP.put(clazz, instance);
    }

    /**
     * 移除单例对象
     *
     * @param clazz 类型
     * @param <T>   泛型
     */
    public static <T> void remove(Class<T> clazz) {
        SINGLETON_MAP.remove(clazz);
    }

    /**
     * 清空所有单例对象
     */
    public static void clear() {
        SINGLETON_MAP.clear();
    }
}