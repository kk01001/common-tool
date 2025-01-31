package io.github.kk01001.examples.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.kk01001.common.dict.annotation.Dict;
import lombok.Data;

@Data
@TableName("t_user")
public class User {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    
    @Dict("user_status")
    private String status;
    
    @Dict("user_type")
    private String type;
    
    private String email;
} 