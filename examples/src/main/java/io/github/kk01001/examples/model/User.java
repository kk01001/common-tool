package io.github.kk01001.examples.model;

import io.github.kk01001.dict.annotation.Dict;
import lombok.Data;

@Data
public class User {
    private String id;
    
    @Dict("user_status")
    private String status;
    
    @Dict("user_type")
    private String type;
} 