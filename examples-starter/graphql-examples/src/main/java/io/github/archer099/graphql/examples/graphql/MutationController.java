package io.github.archer099.graphql.examples.graphql;

import io.github.archer099.graphql.examples.dto.CreateAuthorInput;
import io.github.archer099.graphql.examples.dto.CreateBookInput;
import io.github.archer099.graphql.examples.dto.UpdateAuthorNameInput;
import io.github.archer099.graphql.examples.dto.UpdateBookTitleInput;
import io.github.archer099.graphql.examples.model.Author;
import io.github.archer099.graphql.examples.model.Book;
import io.github.archer099.graphql.examples.service.LibraryService;
import jakarta.validation.Valid;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
public class MutationController {
    private final LibraryService libraryService;

    public MutationController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @MutationMapping
    public Book createBook(@Argument @Valid CreateBookInput input) {
        return libraryService.createBook(input).orElse(null);
    }

    @MutationMapping
    public Book updateBookTitle(@Argument @Valid UpdateBookTitleInput input) {
        return libraryService.updateBookTitle(input).orElse(null);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean deleteBook(@Argument Long id) {
        return libraryService.deleteBook(id);
    }

    @MutationMapping
    public Author createAuthor(@Argument @Valid CreateAuthorInput input) {
        return libraryService.createAuthor(input).orElse(null);
    }

    @MutationMapping
    public Author updateAuthorName(@Argument @Valid UpdateAuthorNameInput input) {
        return libraryService.updateAuthorName(input).orElse(null);
    }

    @MutationMapping
    public Boolean deleteAuthor(@Argument Long id) {
        return libraryService.deleteAuthor(id);
    }
}

