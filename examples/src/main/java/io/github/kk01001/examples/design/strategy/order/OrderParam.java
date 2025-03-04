package io.github.kk01001.examples.design.strategy.order;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderParam {
    private String orderId;
    private String userId;
    private String currentStatus;
    private String operateUser;
    private String remark;
}