package io.github.kk01001.mybatis.permission.handler;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import io.github.kk01001.mybatis.permission.annotations.DataColumn;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author kk01001
 * @date 2024-02-13 14:31:00
 * @description 数据权限处理器工厂
 */
@Component
public class DataPermissionHandlerFactory implements InitializingBean {

    private final Map<String, DataPermissionHandler> handlerMap = new HashMap<>();
    private final ApplicationContext applicationContext;

    public DataPermissionHandlerFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        // 获取所有实现了DataScopeHandler接口的Bean
        Map<String, DataPermissionHandler> beans = applicationContext.getBeansOfType(DataPermissionHandler.class);
        for (DataPermissionHandler handler : beans.values()) {
            handlerMap.put(handler.getType(), handler);
        }
    }

    /**
     * 获取数据权限处理器
     *
     * @param type 数据权限类型
     * @return 数据权限处理器
     */
    public DataPermissionHandler getHandler(String type) {
        return handlerMap.get(type);
    }

    /**
     * 注册数据权限处理器
     *
     * @param handler 数据权限处理器
     */
    public void registerHandler(DataPermissionHandler handler) {
        handlerMap.put(handler.getType(), handler);
    }

    public Optional<String> getSqlCondition(String scopeType, DataColumn dataColumn) {
        DataPermissionHandler handler = getHandler(scopeType);
        if (handler == null) {
            return Optional.empty();
        }

        String condition = handler.getSqlCondition(dataColumn);
        if (StringUtils.isEmpty(condition)) {
            return Optional.empty();
        }
        return Optional.of(condition);
    }

}
