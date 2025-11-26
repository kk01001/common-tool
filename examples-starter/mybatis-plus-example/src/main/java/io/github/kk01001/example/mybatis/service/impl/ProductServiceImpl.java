package io.github.kk01001.example.mybatis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.kk01001.example.mybatis.entity.Product;
import io.github.kk01001.example.mybatis.enums.ProductStatus;
import io.github.kk01001.example.mybatis.mapper.ProductMapper;
import io.github.kk01001.example.mybatis.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 产品服务实现类
 *
 * @author kk01001
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    @Override
    public int batchInsert(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return 0;
        }
        // 使用 starter 提供的批量插入方法
        return productMapper.insertBatchSomeColumn(products);
    }

    @Override
    public Map<String, Object> streamQueryDemo() {
        Map<String, Object> result = new HashMap<>();
        AtomicInteger count = new AtomicInteger(0);
        AtomicInteger totalStock = new AtomicInteger(0);
        List<String> processedNames = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        // 使用流式查询处理数据
        productMapper.selectList(new LambdaQueryWrapper<>(), new ResultHandler<Product>() {
            @Override
            public void handleResult(ResultContext<? extends Product> resultContext) {
                Product product = resultContext.getResultObject();
                int currentCount = count.incrementAndGet();

                // 模拟业务处理
                totalStock.addAndGet(product.getStock() != null ? product.getStock() : 0);

                // 只记录前10条
                if (currentCount <= 10) {
                    processedNames.add(product.getName());
                }

                log.debug("流式查询处理第 {} 条记录: {}", currentCount, product.getName());
            }
        });

        long endTime = System.currentTimeMillis();

        result.put("totalCount", count.get());
        result.put("totalStock", totalStock.get());
        result.put("processedNames", processedNames);
        result.put("executionTime", endTime - startTime);

        return result;
    }

    @Override
    public boolean updateWithOptimisticLock(Long id, Integer stock) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            return false;
        }

        product.setStock(stock);

        // 更新时会自动检查版本号
        // 如果版本号不匹配,更新失败,返回 0
        int rows = productMapper.updateById(product);
        return rows > 0;
    }

    @Override
    public List<Product> getAllProducts() {
        return productMapper.selectList(null);
    }

    @Override
    public Product getProductById(Long id) {
        return productMapper.selectById(id);
    }

    @Override
    public boolean updateProduct(Product product) {
        return productMapper.updateById(product) > 0;
    }

    @Override
    public boolean deleteProduct(Long id) {
        // 逻辑删除,会自动设置 deleted 字段
        return productMapper.deleteById(id) > 0;
    }

    @Override
    public List<Product> generateTestData(int count) {
        List<Product> products = new ArrayList<>();
        Random random = new Random();

        String[] categories = {"笔记本电脑", "手机", "平板", "耳机", "键盘", "鼠标", "显示器", "音箱"};
        String[] brands = {"Apple", "华为", "小米", "联想", "戴尔", "惠普", "索尼", "三星"};

        for (int i = 0; i < count; i++) {
            Product product = new Product();
            product.setName(brands[random.nextInt(brands.length)] + " " +
                    categories[random.nextInt(categories.length)] + " " +
                    (i + 1));
            product.setPrice(new BigDecimal(random.nextInt(10000) + 100));
            product.setStock(random.nextInt(1000));
            product.setVersion(0);

            // 随机设置状态
            ProductStatus[] statuses = ProductStatus.values();
            product.setStatus(statuses[random.nextInt(statuses.length)]);
            product.setDeleted(0);
            products.add(product);
        }

        return products;
    }
}
