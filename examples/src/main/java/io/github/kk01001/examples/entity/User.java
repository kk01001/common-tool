package io.github.kk01001.examples.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.kk01001.common.dict.annotation.Dict;
import io.github.kk01001.crypto.annotation.CryptoField;
import lombok.Data;

@Data
@TableName("t_user")
public class User {
    
    @TableId(type = IdType.AUTO)
    private Long id;

    @CryptoField
    @TableField("username")
    private String username;
    
    @Dict("user_status")
    private String status;
    
    @Dict("user_type")
    private String type;
    
    private String email;
} 