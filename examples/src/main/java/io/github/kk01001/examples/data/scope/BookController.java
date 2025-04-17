package io.github.kk01001.examples.data.scope;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author kk01001
 * @date 2024-05-17 14:30:00
 * @description 图书控制器
 */
@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /**
     * 查询所有图书
     */
    @GetMapping
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    /**
     * 按ID查询图书
     */
    @GetMapping("/{id}")
    public Book getBookById(@PathVariable Long id) {
        return bookService.getById(id);
    }

    /**
     * 按分类查询图书
     */
    @GetMapping("/category/{category}")
    public List<Book> getBooksByCategory(@PathVariable String category) {
        return bookService.getBooksByCategory(category);
    }

    /**
     * 查询图书及创建者信息
     */
    @GetMapping("/with-creator")
    public List<Map<String, Object>> getBooksWithCreator() {
        return bookService.getBooksWithCreator();
    }

    /**
     * 统计各分类图书数量
     */
    @GetMapping("/count-by-category")
    public List<Map<String, Object>> countBooksByCategory() {
        return bookService.countBooksByCategory();
    }

    /**
     * 查询价格高于平均价的图书
     */
    @GetMapping("/price-above-avg")
    public List<Book> getBooksWithPriceAboveAvg() {
        return bookService.getBooksWithPriceAboveAvg();
    }

    /**
     * 查询当前用户创建的图书
     */
    @GetMapping("/my-books")
    public List<Book> getMyBooks(@RequestParam Long userId) {
        return bookService.getMyBooks(userId);
    }

    /**
     * 创建图书
     */
    @PostMapping
    public Book createBook(@RequestBody Book book) {
        book.setCreateTime(LocalDateTime.now());
        book.setUpdateTime(LocalDateTime.now());
        book.setDeleted(0);
        bookService.save(book);
        return book;
    }

    /**
     * 更新图书
     */
    @PutMapping("/{id}")
    public Book updateBook(@PathVariable Long id, @RequestBody Book book) {
        book.setId(id);
        book.setUpdateTime(LocalDateTime.now());
        bookService.updateById(book);
        return book;
    }

    /**
     * 删除图书
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteBook(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        boolean success = bookService.removeById(id);
        result.put("success", success);
        return result;
    }
}
