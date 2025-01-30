package io.github.kk01001.common.dict;

import io.github.kk01001.common.dict.properties.DictProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

@Slf4j
@RequiredArgsConstructor
public class DictRefresher implements ApplicationRunner {
    
    private final DictProperties properties;
    
    @Override
    public void run(ApplicationArguments args) {
        if (properties.getAutoRefresh()) {
            log.info("Start refreshing dictionary cache...");
            DictCache.refreshAll();
            log.info("Dictionary cache refresh completed");
        }
    }
} 