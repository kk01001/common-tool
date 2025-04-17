package io.github.kk01001.examples.data.scope;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @author kk01001
 * @date 2024-05-17 14:30:00
 * @description 图书服务接口
 */
public interface BookService extends IService<Book> {

    /**
     * 查询所有图书
     */
    List<Book> getAllBooks();

    /**
     * 按分类查询图书
     */
    List<Book> getBooksByCategory(String category);

    /**
     * 查询图书及创建者信息
     */
    List<Map<String, Object>> getBooksWithCreator();

    /**
     * 统计各分类图书数量
     */
    List<Map<String, Object>> countBooksByCategory();

    /**
     * 查询价格高于平均价的图书
     */
    List<Book> getBooksWithPriceAboveAvg();

    /**
     * 查询当前用户创建的图书
     */
    List<Book> getMyBooks(Long userId);
}
