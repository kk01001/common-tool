package io.github.archer099.signature.example.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author archer099
 * @date 2026-01-18 13:23:23
 * @description 多级嵌套JSON请求DTO
 */
@Data
public class NestedRequest {

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 用户信息
     */
    private UserInfo user;

    /**
     * 收货地址
     */
    private Address address;

    /**
     * 商品列表
     */
    private List<Product> products;

    /**
     * 总金额
     */
    private BigDecimal totalAmount;

    /**
     * 用户信息
     */
    @Data
    public static class UserInfo {
        /**
         * 用户ID
         */
        private String userId;

        /**
         * 用户名
         */
        private String username;

        /**
         * 手机号
         */
        private String phone;
    }

    /**
     * 地址信息
     */
    @Data
    public static class Address {
        /**
         * 省份
         */
        private String province;

        /**
         * 城市
         */
        private String city;

        /**
         * 详细地址
         */
        private String detail;
    }

    /**
     * 商品信息
     */
    @Data
    public static class Product {
        /**
         * 商品ID
         */
        private String productId;

        /**
         * 商品名称
         */
        private String name;

        /**
         * 数量
         */
        private Integer quantity;

        /**
         * 单价
         */
        private BigDecimal price;
    }
}
