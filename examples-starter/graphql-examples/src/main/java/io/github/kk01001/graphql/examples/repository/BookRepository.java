package io.github.archer099.graphql.examples.repository;

import io.github.archer099.graphql.examples.entity.BookEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookRepository extends JpaRepository<BookEntity, Long> {
    List<BookEntity> findByAuthorId(Long authorId);

    @Query("select new io.github.archer099.graphql.examples.entity.BookEntity(b.id, b.title, b.authorId, b.price, b.publishDate) from BookEntity b")
    Page<BookEntity> findAllWithoutDescription(Pageable pageable);
}

