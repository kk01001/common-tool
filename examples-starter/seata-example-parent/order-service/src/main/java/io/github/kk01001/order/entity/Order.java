package io.github.kk01001.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author linshiqiang
 * @date 2025-01-08 15:25:00
 * @description 订单实体
 */
@Data
@TableName("order_tbl")
@Schema(description = "订单")
public class Order {

    @TableId(type = IdType.AUTO)
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "商品编码")
    private String commodityCode;

    @Schema(description = "数量")
    private Integer count;

    @Schema(description = "金额")
    private Integer money;
}
