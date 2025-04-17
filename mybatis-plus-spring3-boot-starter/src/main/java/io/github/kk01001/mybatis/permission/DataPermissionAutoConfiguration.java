package io.github.kk01001.mybatis.permission;

import io.github.kk01001.mybatis.MybatisPlusConfiguration;
import io.github.kk01001.mybatis.permission.handler.DataPermissionHandlerFactory;
import io.github.kk01001.mybatis.permission.handler.SelfAndSubDataPermissionHandler;
import io.github.kk01001.mybatis.permission.handler.SelfDataPermissionHandler;
import io.github.kk01001.mybatis.permission.interceptor.DataPermissionInterceptor;
import io.github.kk01001.mybatis.permission.service.UserPermissionService;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author kk01001
 * @date 2024-02-13 14:31:00
 * @description 数据权限自动配置类
 */
@AutoConfigureAfter(MybatisPlusConfiguration.class)
@Configuration
@EnableConfigurationProperties(DataPermissionProperties.class)
@ConditionalOnProperty(prefix = "mybatis.data.permission", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DataPermissionAutoConfiguration {

    /**
     * 数据权限处理器工厂
     */
    @Bean
    public DataPermissionHandlerFactory dataScopeHandlerFactory(ApplicationContext applicationContext) {
        return new DataPermissionHandlerFactory(applicationContext);
    }

    /**
     * 部门及子部门数据权限处理器
     */
    @Bean
    @ConditionalOnMissingBean
    public SelfAndSubDataPermissionHandler deptAndSubDataScopeHandler(UserPermissionService userPermissionService) {
        return new SelfAndSubDataPermissionHandler(userPermissionService);
    }

    /**
     * 用户数据权限处理器
     */
    @Bean
    @ConditionalOnMissingBean
    public SelfDataPermissionHandler userDataScopeHandler(UserPermissionService userPermissionService) {
        return new SelfDataPermissionHandler(userPermissionService);
    }

    /**
     * 数据权限拦截器
     */
    @Bean
    @ConditionalOnMissingBean
    public DataPermissionInterceptor dataScopeInterceptor(DataPermissionHandlerFactory handlerFactory,
                                                          DataPermissionProperties properties,
                                                          UserPermissionService userPermissionService) {
        return new DataPermissionInterceptor(properties, handlerFactory, userPermissionService);
    }
}
