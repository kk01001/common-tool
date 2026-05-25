package io.github.archer099.account.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author linshiqiang
 * @date 2025-01-08 15:25:00
 * @description 账户实体
 */
@Data
@TableName("account_tbl")
@Schema(description = "账户")
public class Account {

    @TableId(type = IdType.AUTO)
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "金额")
    private Integer money;
}
