package io.github.archer099.storage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author linshiqiang
 * @date 2025-01-08 15:25:00
 * @description 库存实体
 */
@Data
@TableName("storage_tbl")
@Schema(description = "库存")
public class Storage {

    @TableId(type = IdType.AUTO)
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "商品编码")
    private String commodityCode;

    @Schema(description = "数量")
    private Integer count;
}
