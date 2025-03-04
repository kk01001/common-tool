package io.github.kk01001.examples.design.singleton;

import io.github.kk01001.design.pattern.singleton.SingletonFactory;
import io.github.kk01001.design.pattern.singleton.SingletonPattern;
import lombok.Data;

/**
 * 单例模式示例类
 */
@Data
@SingletonPattern(type = SingletonPattern.Type.LAZY)
public class SingletonExample {

    private String name = "默认名称";
    private int count = 0;

    /**
     * 私有构造方法，防止外部直接实例化
     */
    private SingletonExample() {
        // 私有构造方法，防止外部直接实例化
    }

    /**
     * 获取单例实例
     *
     * @return 单例实例
     */
    public static SingletonExample getInstance() {
        return SingletonFactory.getInstance(SingletonExample.class);
    }

    /**
     * 业务方法示例
     */
    public void incrementCount() {
        count++;
    }
}