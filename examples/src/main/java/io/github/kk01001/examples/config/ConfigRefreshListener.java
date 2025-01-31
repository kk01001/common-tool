package io.github.kk01001.examples.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConfigRefreshListener implements ApplicationListener<EnvironmentChangeEvent> {

    private final ApplicationContext applicationContext;

    public ConfigRefreshListener(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(EnvironmentChangeEvent event) {
        log.info("Configuration changed: {}", event.getKeys());
    }
}