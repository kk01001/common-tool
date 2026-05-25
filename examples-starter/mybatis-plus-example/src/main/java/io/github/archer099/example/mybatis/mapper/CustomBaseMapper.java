package io.github.archer099.example.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.Collection;

/**
 * 自定义 BaseMapper
 * 使用 starter 提供的批量插入方法
 *
 * @author archer099
 */
public interface CustomBaseMapper<T> extends BaseMapper<T> {

    /**
     * 批量插入(使用 starter 提供的 InsertBatchSomeColumn 方法)
     * 方法名必须是 insertBatchSomeColumn
     *
     * @param entityList 实体列表
     * @return 插入的记录数
     */
    int insertBatchSomeColumn(Collection<T> entityList);
}
