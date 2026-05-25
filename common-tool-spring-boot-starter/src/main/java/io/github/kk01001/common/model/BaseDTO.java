package io.github.archer099.common.model;

import lombok.Data;

/**
 * @author archer099
 * date:  2024-07-02 11:38
 */
@Data
public class BaseDTO {

    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新时间
     */
    private String updateBy;

}
