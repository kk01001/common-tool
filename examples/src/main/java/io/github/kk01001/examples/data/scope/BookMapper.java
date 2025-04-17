package io.github.kk01001.examples.data.scope;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.kk01001.mybatis.permission.annotations.DataColumn;
import io.github.kk01001.mybatis.permission.annotations.DataPermission;
import io.github.kk01001.mybatis.permission.handler.DataPermissionType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author kk01001
 * @date 2024-05-17 14:30:00
 * @description 图书Mapper接口
 */
@Mapper
public interface BookMapper extends BaseMapper<Book> {

    /**
     * 简单查询所有图书 限控制
     */
    @DataPermission(type = DataPermissionType.SELF_AND_SUB,
            value = @DataColumn(name = "dept_id"))
    @Select("SELECT * FROM t_book WHERE deleted = 0")
    List<Book> selectAllBooks();

    /**
     * 查询指定分类的图书 演示带条件查询
     */
    @DataPermission(type = DataPermissionType.SELF,
            value = @DataColumn(alias = "t_book", name = "dept_id"))
    @Select("SELECT * FROM t_book WHERE category = #{category} AND deleted = 0")
    List<Book> selectBooksByCategory(@Param("category") String category);

    /**
     * 查询图书及其创建者信息 演示JOIN查询的数据权限
     */
    @DataPermission(type = DataPermissionType.SELF,
            value = @DataColumn(alias = "b", name = "dept_id"))
    @Select("SELECT b.*, u.username as creator_name FROM t_book b LEFT JOIN t_user u ON b.create_user = u.id WHERE b.deleted = 0")
    List<Map<String, Object>> selectBooksWithCreator();

    /**
     * 分组统计各分类 图书数量 演示分组查询的数据权限
     */
    @DataPermission(type = DataPermissionType.SELF,
            value = @DataColumn(alias = "t_book", name = "dept_id"))
    @Select("SELECT category, COUNT(*) as book_count FROM t_book WHERE deleted = 0 GROUP BY category")
    List<Map<String, Object>> countBooksByCategory();

    /**
     * 子查询示例
     * 演示复杂查询的数据权限
     */
    @DataPermission(type = DataPermissionType.SELF,
            value = @DataColumn(alias = "t_book", name = "dept_id"))
    @Select("SELECT * FROM t_book WHERE price > (SELECT AVG(price) FROM t_book) AND deleted = 0")
    List<Book> selectBooksWithPriceAboveAvg();

    /**
     * 自定义条件的数据权限 演示自定义数据权限SQL
     */
    @DataPermission(type = DataPermissionType.CUSTOM, customSql = "create_user = 1")
    @Select("SELECT * FROM t_book WHERE deleted = 0")
    List<Book> selectMyBooks(@Param("userId") Long userId);
}
