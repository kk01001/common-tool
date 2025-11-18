package io.github.kk01001.postgres.examples.mapper;

import io.github.kk01001.postgres.examples.entity.DemoUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DemoUserMapper {

    int insert(DemoUser entity);

    DemoUser selectById(@Param("id") Long id);

    int updateById(DemoUser entity);

    int deleteById(@Param("id") Long id);

    long count(@Param("keyword") String keyword);

    List<DemoUser> page(@Param("keyword") String keyword,
                        @Param("limit") int limit,
                        @Param("offset") int offset);
}