package io.github.kk01001.graphql.examples.service.impl;

import io.github.kk01001.graphql.examples.entity.AuthorEntity;
import io.github.kk01001.graphql.examples.entity.BookEntity;
import io.github.kk01001.graphql.examples.dto.CreateAuthorInput;
import io.github.kk01001.graphql.examples.dto.CreateBookInput;
import io.github.kk01001.graphql.examples.dto.UpdateAuthorNameInput;
import io.github.kk01001.graphql.examples.dto.UpdateBookTitleInput;
import io.github.kk01001.graphql.examples.model.Author;
import io.github.kk01001.graphql.examples.model.Book;
import io.github.kk01001.graphql.examples.model.BookPage;
import io.github.kk01001.graphql.examples.event.BookCreatedEvent;
import io.github.kk01001.graphql.examples.repository.AuthorRepository;
import io.github.kk01001.graphql.examples.repository.BookRepository;
import io.github.kk01001.graphql.examples.service.LibraryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LibraryServiceImpl implements LibraryService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public List<Book> findAllBooks() {
        List<BookEntity> entities = bookRepository.findAll();
        return entities.stream().map(this::toBook).collect(Collectors.toList());
    }

    @Override
    public BookPage findBooks(int page, int size) {
        return findBooks(page, size, true);
    }

    @Override
    public BookPage findBooks(int page, int size, boolean withDescription) {
        // Spring Data JPA uses 0-based page index
        PageRequest pageRequest = PageRequest.of(page - 1, size);
        Page<BookEntity> bookPage;

        if (withDescription) {
            bookPage = bookRepository.findAll(pageRequest);
        } else {
            bookPage = bookRepository.findAllWithoutDescription(pageRequest);
        }

        List<Book> books = bookPage.getContent().stream()
                .map(this::toBook)
                .collect(Collectors.toList());
        return new BookPage(books, bookPage.getTotalElements(), bookPage.getTotalPages());
    }

    @Override
    public Optional<Book> findBookById(Long id) {
        if (Objects.isNull(id)) {
            return Optional.empty();
        }
        return bookRepository.findById(id).map(this::toBook);
    }

    @Override
    public Optional<Author> findAuthorById(Long id) {
        if (Objects.isNull(id)) {
            return Optional.empty();
        }
        return authorRepository.findById(id).map(this::toAuthor);
    }

    @Override
    public List<Author> findAuthorsByIds(List<Long> ids) {
        if (Objects.isNull(ids) || ids.isEmpty()) {
            return List.of();
        }
        return authorRepository.findAllById(ids).stream()
                .map(this::toAuthor)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Author> findAuthorOf(Book book) {
        if (Objects.isNull(book)) {
            return Optional.empty();
        }
        return authorRepository.findById(book.getAuthorId()).map(this::toAuthor);
    }

    @Override
    @Transactional
    public Optional<Book> createBook(CreateBookInput input) {
        if (Objects.isNull(input) || Objects.isNull(input.getId()) || Objects.isNull(input.getAuthorId())) {
            return Optional.empty();
        }
        if ("error".equalsIgnoreCase(input.getTitle())) {
            throw new io.github.kk01001.graphql.examples.exception.BusinessException("Title 'error' is not allowed!");
        }
        BookEntity entity = new BookEntity();
        entity.setId(input.getId());
        entity.setTitle(input.getTitle());
        entity.setAuthorId(input.getAuthorId());
        entity.setPublishDate(input.getPublishDate() != null ? input.getPublishDate().toLocalDateTime() : null);
        bookRepository.save(entity);
        Book book = toBook(entity);
        eventPublisher.publishEvent(new BookCreatedEvent(this, book));
        return Optional.of(book);
    }

    @Override
    @Transactional
    public Optional<Book> updateBookTitle(UpdateBookTitleInput input) {
        if (Objects.isNull(input) || Objects.isNull(input.getId())) {
            return Optional.empty();
        }
        return bookRepository.findById(input.getId()).map(e -> {
            e.setTitle(input.getTitle());
            bookRepository.save(e);
            return toBook(e);
        });
    }

    @Override
    @Transactional
    public boolean deleteBook(Long id) {
        if (Objects.isNull(id)) {
            return false;
        }
        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public Optional<Author> createAuthor(CreateAuthorInput input) {
        if (Objects.isNull(input) || Objects.isNull(input.getId())) {
            return Optional.empty();
        }
        AuthorEntity entity = new AuthorEntity();
        entity.setId(input.getId());
        entity.setName(input.getName());
        authorRepository.save(entity);
        return Optional.of(toAuthor(entity));
    }

    @Override
    @Transactional
    public Optional<Author> updateAuthorName(UpdateAuthorNameInput input) {
        if (Objects.isNull(input) || Objects.isNull(input.getId())) {
            return Optional.empty();
        }
        return authorRepository.findById(input.getId()).map(e -> {
            e.setName(input.getName());
            authorRepository.save(e);
            return toAuthor(e);
        });
    }

    @Override
    @Transactional
    public boolean deleteAuthor(Long id) {
        if (Objects.isNull(id)) {
            return false;
        }
        if (authorRepository.existsById(id)) {
            authorRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private Book toBook(BookEntity e) {
        return new Book(e.getId(), e.getTitle(), e.getAuthorId(), e.getDescription(), e.getPrice(),
                e.getPublishDate() != null ? e.getPublishDate().atOffset(java.time.ZoneOffset.UTC) : null);
    }

    private Author toAuthor(AuthorEntity e) {
        return new Author(e.getId(), e.getName());
    }
}
