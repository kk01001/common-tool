package io.github.archer099.graphql.examples.graphql;

import graphql.schema.DataFetchingFieldSelectionSet;
import io.github.archer099.graphql.examples.model.Author;
import io.github.archer099.graphql.examples.model.Book;
import io.github.archer099.graphql.examples.model.BookPage;
import io.github.archer099.graphql.examples.service.LibraryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@Slf4j
public class BookController {
    private final LibraryService libraryService;

    public BookController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @QueryMapping
    public BookPage books(@Argument int page, @Argument int size, DataFetchingFieldSelectionSet selectionSet) {
        boolean withDescription = selectionSet.contains("content/description");
        if (withDescription) {
            log.info("📝 User requested 'description' field. Performing heavy text retrieval...");
        } else {
            log.info("⚡ Optimization: 'description' not requested. Skipping heavy text retrieval.");
        }
        
        return libraryService.findBooks(page, size, withDescription);
    }

    @QueryMapping
    public Book bookById(@Argument Long id) {
        return libraryService.findBookById(id).orElse(null);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public String me(Principal principal) {
        return "Current User ID: " + principal.getName();
    }

    @BatchMapping
    public Map<Book, Author> author(List<Book> books) {
        // 1. 收集所有 Author ID
        List<Long> authorIds = books.stream()
                .map(Book::getAuthorId)
                .collect(Collectors.toList());

        // 2. 批量查询 Authors
        Map<Long, Author> authorMap = libraryService.findAuthorsByIds(authorIds).stream()
                .collect(Collectors.toMap(Author::getId, Function.identity()));

        // 3. 组装结果 Map<Book, Author>
        return books.stream()
                .collect(Collectors.toMap(Function.identity(), book -> authorMap.get(book.getAuthorId())));
    }

    @SchemaMapping
    public String formattedTitle(Book book) {
        return String.format("《%s》", book.getTitle());
    }
}

