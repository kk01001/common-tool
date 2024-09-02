package io.github.kk01001.mybatis.datasource;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.baomidou.dynamic.datasource.destroyer.DataSourceDestroyer;
import com.baomidou.dynamic.datasource.destroyer.DefaultDataSourceDestroyer;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Objects;

/**
 * @author kk01001
 * @date 2024-08-17 10:09:00
 * @description
 */
@Slf4j
@RequiredArgsConstructor
public class DataSourceManager {

    private final DynamicRoutingDataSource dynamicRoutingDataSource;

    private final DynamicDataSourceProperties dynamicDataSourceProperties;

    private final DefaultDataSourceCreator defaultDataSourceCreator;

    /**
     * 比较nacos数据库连接信息变化, 再更新数据源
     *
     * @param datasourceName 数据源名称
     */
    public void updateDataSource(String datasourceName) {
        DataSource oldDataSource = dynamicRoutingDataSource.getDataSource(datasourceName);
        if (Objects.isNull(oldDataSource)) {
            return;
        }
        Map<String, DataSourceProperty> datasourceMap = dynamicDataSourceProperties.getDatasource();
        if (CollectionUtils.isEmpty(datasourceMap)) {
            return;
        }
        DataSourceProperty dataSourceProperty = datasourceMap.get(datasourceName);
        boolean isNoChanged = companyDatasource(oldDataSource, dataSourceProperty);
        if (!isNoChanged) {
            // close old
            closeDataSource(datasourceName, oldDataSource);

            // add new
            DataSource newDataSource = defaultDataSourceCreator.createDataSource(dataSourceProperty);
            dynamicRoutingDataSource.addDataSource(datasourceName, newDataSource);
        }
    }

    private void closeDataSource(String ds, DataSource dataSource) {
        try {
            DataSourceDestroyer destroyer = new DefaultDataSourceDestroyer();
            destroyer.asyncDestroy(ds, dataSource);
        } catch (Exception e) {
            log.warn("dynamic-datasource closed datasource named [{}] failed", ds, e);
        }
    }

    private boolean companyDatasource(DataSource dataSource, DataSourceProperty dataSourceProperty) {
        String url = ReflectionDataSourceUtils.getUrl(dataSource);
        String username = ReflectionDataSourceUtils.getUsername(dataSource);
        String password = ReflectionDataSourceUtils.getPassword(dataSource);
        String type = ReflectionDataSourceUtils.getType(dataSource);
        // 比较从 DataSource 获取到的值与 DataSourceProperty 中的值
        return url.equals(dataSourceProperty.getUrl()) &&
                type.equals(dataSourceProperty.getType().getName()) &&
                username.equals(dataSourceProperty.getUsername()) &&
                password.equals(dataSourceProperty.getPassword());
    }

    public void addDataSource(String datasourceName, DataSourceProperty dataSourceProperty) {
        // 实现添加数据源的逻辑
    }

    public void removeDataSource(String datasourceName) {
        // 实现删除数据源的逻辑
    }

}
