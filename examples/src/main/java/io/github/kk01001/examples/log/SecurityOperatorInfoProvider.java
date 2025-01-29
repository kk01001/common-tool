package io.github.kk01001.examples.log;

import io.github.kk01001.common.log.service.OperatorInfoProvider;

public class SecurityOperatorInfoProvider implements OperatorInfoProvider {

    @Override
    public String getOperatorId() {
        return "11111";
    }

    @Override
    public String getOperatorName() {
        return "name";
    }

    @Override
    public String getOperatorDeptId() {
        // 从用户上下文中获取部门ID
        return "444444";
    }

    @Override
    public String getOperatorDeptName() {
        // 从用户上下文中获取部门名称
        return "55555";
    }
}