package io.github.kk01001.common.dict.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DictModule extends Module {

    private final Module module;

    @Override
    public String getModuleName() {
        return "DictModule";
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