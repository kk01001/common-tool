package io.github.kk01001.graphql.examples.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateBookInput {
    @NotNull
    private Long id;
    @NotBlank
    private String title;
    @NotNull
    private Long authorId;
    
    private java.time.OffsetDateTime publishDate;

    private String coverImage;
}

