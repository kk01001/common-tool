package io.github.kk01001.storage.controller;

import io.github.kk01001.seata.common.result.Result;
import io.github.kk01001.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/storage")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    @PostMapping("/deduct")
    public Result<Void> deduct(@RequestParam String commodityCode, @RequestParam Integer count) {
        storageService.deduct(commodityCode, count);
        return Result.success();
    }
}
