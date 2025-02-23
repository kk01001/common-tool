package io.github.kk01001.examples.idempotent;

import io.github.kk01001.idempotent.aspect.Idempotent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author linshiqiang
 * @date 2024-12-03 14:00:00
 * @description
 */
@RestController
public class IdempotentController {

    @Idempotent(keyPrefix = "IdempotentTest",
            expire = 1)
    @PostMapping("Idempotent")
    public Boolean ok(@RequestBody Map<String, String> params) {

        return true;
    }

    public static String hello() {
        return "dddd";
    }
}
