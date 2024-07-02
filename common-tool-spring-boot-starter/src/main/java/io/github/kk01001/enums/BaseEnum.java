package io.github.kk01001.enums;

/**
 * @author kk01001
 * date:  2023-07-21 10:05
 */
public interface BaseEnum<T extends Enum<T> & BaseEnum<T>> {

    /**
     * 根据code查询枚举类型
     *
     * @param clazz 枚举类
     * @param code  code
     * @param <T>   枚举类型
     * @return 枚举
     */
    @SuppressWarnings("all")
    static <T extends Enum<T> & BaseEnum<T>> T parseByCode(Class<T> clazz, Integer code) {
        Enum[] enums = clazz.getEnumConstants();
        int length = enums.length;
        for (Enum e : enums) {
            T t = (T) e;
            if (t.getCode().equals(code)) {
                return t;
            }
        }
        return null;
    }

    /**
     * code
     *
     * @return code
     */
    Integer getCode();

    /**
     * name
     *
     * @return name
     */
    String getName();
}
