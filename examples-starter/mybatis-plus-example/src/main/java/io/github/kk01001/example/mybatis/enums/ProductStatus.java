package io.github.archer099.example.mybatis.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 产品状态枚举
 * 演示 MyBatis Plus 枚举自动转换功能
 *
 * @author archer099
 */
@Getter
public enum ProductStatus {

    /**
     * 在售
     */
    AVAILABLE(1, "在售"),

    /**
     * 缺货
     */
    OUT_OF_STOCK(2, "缺货"),

    /**
     * 停售
     */
    DISCONTINUED(3, "停售");

    /**
     * 存储到数据库的值
     */
    @EnumValue
    private final Integer code;

    /**
     * 显示的描述
     */
    @JsonValue
    private final String description;

    ProductStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
}
