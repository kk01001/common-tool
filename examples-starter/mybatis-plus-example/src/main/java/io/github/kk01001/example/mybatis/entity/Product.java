package io.github.archer099.example.mybatis.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.github.archer099.example.mybatis.enums.ProductStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 产品实体类
 * 用于演示乐观锁、批量操作、枚举转换等高级功能
 *
 * @author archer099
 */
@Data
@TableName("product")
public class Product {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 产品名称
     */
    private String name;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 版本号(乐观锁)
     */
    @Version
    private Integer version;

    /**
     * 产品状态(枚举)
     */
    private ProductStatus status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标志
     */
    @TableLogic
    private Integer deleted;
}
