package io.github.kk01001.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.kk01001.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
