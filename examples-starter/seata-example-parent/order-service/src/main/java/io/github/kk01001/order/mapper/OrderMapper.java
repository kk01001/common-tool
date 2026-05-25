package io.github.archer099.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.archer099.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
