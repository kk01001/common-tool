package io.github.archer099.graphql.examples.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Book {
    private Long id;
    private String title;
    private Long authorId;
    private String description;
    private Double price;
    private java.time.OffsetDateTime publishDate;
    private String coverImage;
}

