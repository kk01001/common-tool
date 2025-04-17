package io.github.kk01001.examples.data.scope;

import io.github.kk01001.mybatis.permission.handler.DataPermissionHandlerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kk01001
 * @date 2024-05-17 14:30:00
 * @description 图书数据权限测试控制器
 */
@Slf4j
@RestController
@RequestMapping("/test/data-scope")
@RequiredArgsConstructor
public class BookDataScopeTestController {

    private final BookService bookService;
    private final JdbcTemplate jdbcTemplate;
    private final DataPermissionHandlerFactory dataPermissionHandlerFactory;

    /**
     * 测试各种SQL查询的数据权限效果
     */
    @GetMapping("/book-sql-examples")
    public Map<String, Object> testBookDataScope() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 测试简单查询
            log.info("=== 测试简单查询 ===");
            bookService.getAllBooks();
            log.info("===================================");

            // 测试带条件查询
            log.info("=== 测试带条件查询 ===");
            bookService.getBooksByCategory("计算机科学");
            log.info("===================================");

            // 测试JOIN查询
            log.info("=== 测试JOIN查询 ===");
            bookService.getBooksWithCreator();
            log.info("===================================");

            // 测试分组统计查询
            log.info("=== 测试分组统计查询 ===");
            bookService.countBooksByCategory();
            log.info("===================================");

            // 测试子查询
            log.info("=== 测试子查询 ===");
            bookService.getBooksWithPriceAboveAvg();
            log.info("===================================");

            // 测试自定义数据权限
            log.info("=== 测试自定义数据权限 ===");
            bookService.getMyBooks(1L);
            log.info("===================================");

            result.put("success", true);
            result.put("message", "数据权限测试完成，请查看日志");
        } catch (Exception e) {
            log.error("测试数据权限异常", e);
            result.put("success", false);
            result.put("message", "测试数据权限异常: " + e.getMessage());
            result.put("error", e.toString());
        }

        return result;
    }
}
