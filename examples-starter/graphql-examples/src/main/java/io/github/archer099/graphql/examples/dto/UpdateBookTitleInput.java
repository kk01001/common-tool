package io.github.archer099.graphql.examples.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateBookTitleInput {
    @NotNull
    private Long id;
    @NotBlank
    private String title;
}

