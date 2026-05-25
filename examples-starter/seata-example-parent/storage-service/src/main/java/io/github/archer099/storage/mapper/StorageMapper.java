package io.github.archer099.storage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.archer099.storage.entity.Storage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StorageMapper extends BaseMapper<Storage> {
}
