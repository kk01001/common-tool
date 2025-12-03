package io.github.kk01001.graphql.examples.service;

import io.github.kk01001.graphql.examples.model.Author;
import io.github.kk01001.graphql.examples.model.Book;

import io.github.kk01001.graphql.examples.model.BookPage;
import java.util.List;
import java.util.Optional;

public interface LibraryService {
    List<Book> findAllBooks();
    BookPage findBooks(int page, int size);
    BookPage findBooks(int page, int size, boolean withDescription);
    Optional<Book> findBookById(Long id);
    Optional<Author> findAuthorById(Long id);
    List<Author> findAuthorsByIds(List<Long> ids);
    Optional<Author> findAuthorOf(Book book);

    Optional<Book> createBook(io.github.kk01001.graphql.examples.dto.CreateBookInput input);
    Optional<Book> updateBookTitle(io.github.kk01001.graphql.examples.dto.UpdateBookTitleInput input);
    boolean deleteBook(Long id);

    Optional<Author> createAuthor(io.github.kk01001.graphql.examples.dto.CreateAuthorInput input);
    Optional<Author> updateAuthorName(io.github.kk01001.graphql.examples.dto.UpdateAuthorNameInput input);
    boolean deleteAuthor(Long id);
}
