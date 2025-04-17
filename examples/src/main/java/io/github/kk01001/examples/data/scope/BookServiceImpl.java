package io.github.kk01001.examples.data.scope;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author kk01001
 * @date 2024-05-17 14:30:00
 * @description 图书服务实现类
 */
@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements BookService {

    @Override
    public List<Book> getAllBooks() {
        return this.baseMapper.selectAllBooks();
    }

    @Override
    public List<Book> getBooksByCategory(String category) {
        return this.baseMapper.selectBooksByCategory(category);
    }

    @Override
    public List<Map<String, Object>> getBooksWithCreator() {
        return this.baseMapper.selectBooksWithCreator();
    }

    @Override
    public List<Map<String, Object>> countBooksByCategory() {
        return this.baseMapper.countBooksByCategory();
    }

    @Override
    public List<Book> getBooksWithPriceAboveAvg() {
        return this.baseMapper.selectBooksWithPriceAboveAvg();
    }

    @Override
    public List<Book> getMyBooks(Long userId) {
        return this.baseMapper.selectMyBooks(userId);
    }
}
