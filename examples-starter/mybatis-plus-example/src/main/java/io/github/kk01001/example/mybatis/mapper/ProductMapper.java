package io.github.kk01001.example.mybatis.mapper;

import io.github.kk01001.example.mybatis.entity.Product;
import org.apache.ibatis.annotations.Mapper;

/**
 * 产品 Mapper 接口
 *
 * @author kk01001
 */
@Mapper
public interface ProductMapper extends CustomBaseMapper<Product> {
}
