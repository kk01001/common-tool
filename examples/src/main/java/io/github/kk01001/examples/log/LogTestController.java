package io.github.kk01001.examples.log;

import io.github.kk01001.common.log.annotation.OperationLog;
import io.github.kk01001.common.log.annotation.OperationType;
import io.github.kk01001.common.model.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/log")
public class LogTestController {

    @OperationLog(type = OperationType.CREATE, description = "创建用户")
    @PostMapping
    public ApiResponse<Map<String, String>> createUser(@RequestBody Map<String, String> params) {
        // 创建用户逻辑
        return ApiResponse.ok(params);
    }
}