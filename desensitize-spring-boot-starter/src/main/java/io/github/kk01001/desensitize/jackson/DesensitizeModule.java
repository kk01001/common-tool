package io.github.archer099.desensitize.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import lombok.RequiredArgsConstructor;

/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description
 */
@RequiredArgsConstructor
public class DesensitizeModule extends Module {
    
    private final Module module;
    
    @Override
    public String getModuleName() {
        return "DesensitizeModule";
    }
    
    @Override
    public Version version() {
        return Version.unknownVersion();
    }
    
    @Override
    public void setupModule(SetupContext context) {
        module.setupModule(context);
    }
} 