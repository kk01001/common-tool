package io.github.kk01001.storage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.kk01001.storage.entity.Storage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StorageMapper extends BaseMapper<Storage> {
}
