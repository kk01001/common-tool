package io.github.archer099.example.mybatis.controller;

import io.github.archer099.example.mybatis.entity.Product;
import io.github.archer099.example.mybatis.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 高级功能演示控制器
 *
 * @author archer099
 */
@RestController
@RequestMapping("/api/advanced")
@RequiredArgsConstructor
public class AdvancedFeaturesController {

    private final ProductService productService;

    /**
     * 批量插入演示
     */
    @PostMapping("/batch-insert")
    public Map<String, Object> batchInsert(@RequestParam(defaultValue = "100") int count) {
        long startTime = System.currentTimeMillis();
        
        // 生成测试数据
        List<Product> products = productService.generateTestData(count);
        
        // 批量插入
        int rows = productService.batchInsert(products);
        
        long endTime = System.currentTimeMillis();
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("insertedRows", rows);
        result.put("executionTime", endTime - startTime);
        result.put("message", "成功批量插入 " + rows + " 条记录,耗时 " + (endTime - startTime) + "ms");
        
        return result;
    }

    /**
     * 流式查询演示
     */
    @GetMapping("/stream-query")
    public Map<String, Object> streamQuery() {
        Map<String, Object> result = productService.streamQueryDemo();
        result.put("success", true);
        result.put("message", "流式查询完成");
        return result;
    }

    /**
     * 乐观锁更新演示
     */
    @PostMapping("/optimistic-lock")
    public Map<String, Object> optimisticLockDemo(@RequestParam Long id, @RequestParam Integer stock) {
        boolean success = productService.updateWithOptimisticLock(id, stock);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "更新成功" : "更新失败,可能是版本号冲突");
        
        return result;
    }

    /**
     * 查询所有产品
     */
    @GetMapping("/products")
    public Map<String, Object> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", products);
        result.put("total", products.size());
        
        return result;
    }

    /**
     * 根据ID查询产品
     */
    @GetMapping("/products/{id}")
    public Map<String, Object> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", product != null);
        result.put("data", product);
        
        return result;
    }

    /**
     * 更新产品
     */
    @PutMapping("/products/{id}")
    public Map<String, Object> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        boolean success = productService.updateProduct(product);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "更新成功" : "更新失败,可能是版本号冲突");
        
        return result;
    }

    /**
     * 逻辑删除产品
     */
    @DeleteMapping("/products/{id}")
    public Map<String, Object> deleteProduct(@PathVariable Long id) {
        boolean success = productService.deleteProduct(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "删除成功(逻辑删除)" : "删除失败");
        
        return result;
    }

    /**
     * 清空所有产品数据
     */
    @DeleteMapping("/products/clear")
    public Map<String, Object> clearAllProducts() {
        List<Product> products = productService.getAllProducts();
        int count = 0;
        for (Product product : products) {
            if (productService.deleteProduct(product.getId())) {
                count++;
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("deletedCount", count);
        result.put("message", "已清空 " + count + " 条记录");
        
        return result;
    }
}
