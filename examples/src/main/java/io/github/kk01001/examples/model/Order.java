package io.github.archer099.examples.model;

import io.github.archer099.dict.annotation.Dict;
import lombok.Data;

@Data
public class Order {
    private String id;
    
    @Dict(value = "order_status", suffix = "Name")
    private String status;
    
    @Dict("pay_type")
    private String payType;
    
    private Double amount;
} 