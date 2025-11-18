package io.github.kk01001.postgres.examples.controller;

import io.github.kk01001.common.model.ApiResponse;
import io.github.kk01001.postgres.examples.dto.CreateUserDTO;
import io.github.kk01001.postgres.examples.dto.IdDTO;
import io.github.kk01001.postgres.examples.dto.PageQueryDTO;
import io.github.kk01001.postgres.examples.dto.UpdateUserDTO;
import io.github.kk01001.postgres.examples.service.PostgresDemoService;
import io.github.kk01001.postgres.examples.vo.PageResultVO;
import io.github.kk01001.postgres.examples.vo.UserVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/postgres")
@Slf4j
public class PostgresController {

    private final PostgresDemoService demoService;

    @PostMapping("/init")
    @Schema(description = "初始化表 demo_user")
    public ApiResponse<Void> init() {
        demoService.initTable();
        return ApiResponse.ok();
    }

    @PostMapping("/create")
    @Schema(description = "新增用户")
    public ApiResponse<Long> create(@Valid @RequestBody CreateUserDTO dto) {
        Long id = demoService.createUser(dto);
        return ApiResponse.ok(id);
    }

    @PostMapping("/get")
    @Schema(description = "根据ID查询用户")
    public ApiResponse<UserVO> get(@Valid @RequestBody IdDTO dto) {
        UserVO vo = demoService.getUser(dto);
        return ApiResponse.ok(vo);
    }

    @PostMapping("/update")
    @Schema(description = "更新用户信息")
    public ApiResponse<Boolean> update(@Valid @RequestBody UpdateUserDTO dto) {
        Boolean ok = demoService.updateUser(dto);
        return ApiResponse.ok(ok);
    }

    @PostMapping("/delete")
    @Schema(description = "删除用户")
    public ApiResponse<Boolean> delete(@Valid @RequestBody IdDTO dto) {
        Boolean ok = demoService.deleteUser(dto);
        return ApiResponse.ok(ok);
    }

    @PostMapping("/page")
    @Schema(description = "分页查询用户")
    public ApiResponse<PageResultVO<UserVO>> page(@Valid @RequestBody PageQueryDTO dto) {
        PageResultVO<UserVO> page = demoService.pageUsers(dto);
        return ApiResponse.ok(page);
    }

    @PostMapping("/tx-demo")
    @Schema(description = "事务演示：插入后抛异常回滚")
    public ApiResponse<Void> txDemo() {
        try {
            demoService.transactionalRollbackDemo();
            return ApiResponse.ok();
        } catch (Exception e) {
            return ApiResponse.ok();
        }
    }
}