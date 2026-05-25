package io.github.archer099.example.mybatis.service;

import io.github.archer099.example.mybatis.entity.Product;

import java.util.List;
import java.util.Map;

/**
 * 产品服务接口
 *
 * @author archer099
 */
public interface ProductService {

    /**
     * 批量插入产品(使用自定义 SQL 注入器)
     *
     * @param products 产品列表
     * @return 插入的记录数
     */
    int batchInsert(List<Product> products);

    /**
     * 流式查询演示
     * 模拟大数据量处理
     *
     * @return 处理结果统计
     */
    Map<String, Object> streamQueryDemo();

    /**
     * 乐观锁更新演示
     *
     * @param id 产品ID
     * @param stock 新库存
     * @return 是否更新成功
     */
    boolean updateWithOptimisticLock(Long id, Integer stock);

    /**
     * 查询所有产品
     *
     * @return 产品列表
     */
    List<Product> getAllProducts();

    /**
     * 根据ID查询产品
     *
     * @param id 产品ID
     * @return 产品信息
     */
    Product getProductById(Long id);

    /**
     * 更新产品
     *
     * @param product 产品信息
     * @return 是否成功
     */
    boolean updateProduct(Product product);

    /**
     * 逻辑删除产品
     *
     * @param id 产品ID
     * @return 是否成功
     */
    boolean deleteProduct(Long id);

    /**
     * 生成测试数据
     *
     * @param count 数据条数
     * @return 生成的产品列表
     */
    List<Product> generateTestData(int count);
}
