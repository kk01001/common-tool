package io.github.kk01001.graphql.examples.repository;

import io.github.kk01001.graphql.examples.entity.AuthorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<AuthorEntity, Long> {
}

