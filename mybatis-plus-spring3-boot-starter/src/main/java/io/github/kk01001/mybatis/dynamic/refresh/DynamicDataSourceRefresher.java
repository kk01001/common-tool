package io.github.kk01001.mybatis.dynamic.refresh;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DataSourceCreator;
import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class DynamicDataSourceRefresher implements SmartInitializingSingleton {

    private final DataSource dataSource;
    private final DynamicDataSourceProperties properties;
    private final DataSourceCreator dataSourceCreator;

    // 缓存上一次的数据源配置
    private final Map<String, DataSourceProperty> lastDataSourceProps = new ConcurrentHashMap<>();

    @Override
    public void afterSingletonsInstantiated() {
        if (!(dataSource instanceof DynamicRoutingDataSource)) {
            log.warn("DataSource is not instance of DynamicRoutingDataSource, refresh will be disabled");
            return;
        }
        // 初始化完成后检查数据源配置
        refreshDataSourceIfNeeded(properties);

        // 缓存当前配置
        Map<String, DataSourceProperty> currentDataSourceMap = properties.getDatasource();
        currentDataSourceMap.forEach((ds, prop) -> {
            lastDataSourceProps.put(ds, copyDataSourceProperty(prop));
            log.debug("Cached initial datasource config: {}", ds);
        });

    }

    public void refreshDataSourceIfNeeded(DynamicDataSourceProperties properties) {
        DynamicRoutingDataSource dynamicDataSource = (DynamicRoutingDataSource) dataSource;
        Map<String, DataSourceProperty> currentDataSourceMap = properties.getDatasource();

        // 检查每个数据源的配置是否发生变化
        for (Map.Entry<String, DataSourceProperty> entry : currentDataSourceMap.entrySet()) {
            String ds = entry.getKey();
            DataSourceProperty currentProp = entry.getValue();
            DataSourceProperty lastProp = lastDataSourceProps.get(ds);

            if (!isDataSourceChanged(lastProp, currentProp)) {
                continue;
            }

            try {
                // 获取旧的数据源并关闭
                DataSource oldDataSource = dynamicDataSource.getDataSource(ds);
                if (oldDataSource instanceof AutoCloseable) {
                    try {
                        ((AutoCloseable) oldDataSource).close();
                        log.info("Closed old datasource: {}", ds);
                    } catch (Exception e) {
                        log.error("Failed to close old datasource: {}", ds, e);
                    }
                }
                // 创建新的数据源
                DataSource newDataSource = dataSourceCreator.createDataSource(currentProp);

                // 替换为新的数据源
                dynamicDataSource.addDataSource(ds, newDataSource);

                // 更新缓存
                lastDataSourceProps.put(ds, copyDataSourceProperty(currentProp));
                log.info("Refreshed datasource: {}", ds);
            } catch (Exception e) {
                log.error("Failed to refresh datasource: {}", ds, e);
            }
        }

    }

    private boolean isDataSourceChanged(DataSourceProperty lastProp, DataSourceProperty currentProp) {
        if (lastProp == null) {
            return true;
        }

        return !Objects.equals(lastProp.getUrl(), currentProp.getUrl()) ||
                !Objects.equals(lastProp.getUsername(), currentProp.getUsername()) ||
                !Objects.equals(lastProp.getPassword(), currentProp.getPassword()) ||
                !Objects.equals(lastProp.getDriverClassName(), currentProp.getDriverClassName());
    }

    private DataSourceProperty copyDataSourceProperty(DataSourceProperty source) {
        DataSourceProperty copy = new DataSourceProperty();
        copy.setPoolName(source.getPoolName());
        copy.setUrl(source.getUrl());
        copy.setUsername(source.getUsername());
        copy.setPassword(source.getPassword());
        copy.setDriverClassName(source.getDriverClassName());
        return copy;
    }
} 