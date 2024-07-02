package io.github.kk01001.controller;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author kk01001
 * @since 2022/6/24 9:54
 */
@RestController
public class VersionController {

    private static final Map<String, String> VERSION_MAP = new HashMap<>(1);

    private static final String VERSION = "version";

    /**
     * 获取版本信息接口
     */
    @GetMapping(value = "/get-version")
    public String getVersion() throws IOException {
        if (StrUtil.isNotEmpty(VERSION_MAP.get(VERSION))) {

            return VERSION_MAP.get(VERSION);
        }
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("version.json");
        Resource resource = resources[0];
        String version = IoUtil.readUtf8(resource.getInputStream());
        VERSION_MAP.put(VERSION, version);
        return version;
    }

    /**
     * 检测ping
     */
    @GetMapping(value = "/ping")
    public String ping() {
        return HttpStatus.OK.getReasonPhrase();
    }
}
