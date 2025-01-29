package io.github.kk01001.common.log.service;

/**
 * 操作人信息提供接口
 */
public interface OperatorInfoProvider {
    
    /**
     * 获取操作人ID
     */
    String getOperatorId();
    
    /**
     * 获取操作人名称
     */
    String getOperatorName();
    
    /**
     * 获取操作人部门ID
     */
    String getOperatorDeptId();
    
    /**
     * 获取操作人部门名称
     */
    String getOperatorDeptName();
    
    /**
     * 默认实现
     */
    OperatorInfoProvider DEFAULT = new OperatorInfoProvider() {
        @Override
        public String getOperatorId() {
            return "unknown";
        }
        
        @Override
        public String getOperatorName() {
            return "unknown";
        }
        
        @Override
        public String getOperatorDeptId() {
            return "unknown";
        }
        
        @Override
        public String getOperatorDeptName() {
            return "unknown";
        }
    };
} 