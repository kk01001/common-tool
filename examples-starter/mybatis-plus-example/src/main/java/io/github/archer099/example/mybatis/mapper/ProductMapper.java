package io.github.archer099.example.mybatis.mapper;

import io.github.archer099.example.mybatis.entity.Product;
import org.apache.ibatis.annotations.Mapper;

/**
 * 产品 Mapper 接口
 *
 * @author archer099
 */
@Mapper
public interface ProductMapper extends CustomBaseMapper<Product> {
}
