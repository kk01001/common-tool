package io.github.kk01001.graphql.examples.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
public class BookEntity {
    @Id
    private Long id;
    private String title;

    @Column(name = "author_id")
    private Long authorId;

    // Simulated heavy field
    @Column(name = "description")
    private String description;

    @Column(name = "price")
    private Double price;

    @Column(name = "publish_date")
    private java.time.LocalDateTime publishDate;

    @Column(name = "cover_image")
    private String coverImage;

    public BookEntity(Long id, String title, Long authorId, Double price, java.time.LocalDateTime publishDate) {
        this.id = id;
        this.title = title;
        this.authorId = authorId;
        this.price = price;
        this.publishDate = publishDate;
    }
}

