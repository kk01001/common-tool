package io.github.kk01001.examples.design.singleton;

import io.github.kk01001.design.pattern.singleton.SingletonFactory;
import io.github.kk01001.design.pattern.singleton.SingletonPattern;

/**
 * @author linshiqiang
 * @date 2025-03-04 10:02:00
 * @description
 */
@SingletonPattern(type = SingletonPattern.Type.ENUM)
public enum EnumSingletonExample {

    INSTANCE;

    public String hello() {
        return "hello";
    }

    public static void main(String[] args) {
        EnumSingletonExample instance = SingletonFactory.getInstance(EnumSingletonExample.class);
        System.out.println(instance.hello());
    }
}
