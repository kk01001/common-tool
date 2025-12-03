package io.github.kk01001.graphql.examples.graphql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.graphql.test.tester.WebGraphQlTester;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "logging.level.org.springframework.web=DEBUG")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BookControllerTest {

    @LocalServerPort
    int port;

    @Autowired
    private WebTestClient webTestClient;

    private HttpGraphQlTester graphQlTester;

    @BeforeEach
    void setUp() {
        this.graphQlTester = HttpGraphQlTester.builder(
                webTestClient.mutate()
                    .baseUrl("http://localhost:" + port + "/graphql")
                )
                .build();
    }

    @Test
    void checkGraphiql() {
        webTestClient.get().uri("/graphiql?path=/graphql")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldGetBooks() {
        String document = """
            query {
                books(page: 1, size: 5) {
                    content {
                        id
                        title
                        publishDate
                    }
                    totalElements
                }
            }
            """;

        graphQlTester.document(document)
                .execute()
                .path("books.content")
                .entityList(Object.class)
                .hasSize(3);
    }

    @Test
    void shouldHandleException() {
        String document = """
            mutation {
                createBook(input: {
                    id: 999,
                    title: "error",
                    authorId: 1,
                    publishDate: "2023-01-01T00:00:00Z"
                }) {
                    id
                    title
                }
            }
            """;

        graphQlTester.document(document)
                .execute()
                .errors()
                .expect(error -> error.getMessage().equals("Title 'error' is not allowed!"))
                .verify();
    }

    @Test
    void shouldCreateBookWithDate() {
        String document = """
            mutation {
                createBook(input: {
                    id: 888,
                    title: "New Book",
                    authorId: 1,
                    publishDate: "2023-10-01T12:00:00Z"
                }) {
                    id
                    title
                    publishDate
                }
            }
            """;

        graphQlTester.document(document)
                .execute()
                .path("createBook.publishDate")
                .entity(OffsetDateTime.class)
                .isEqualTo(OffsetDateTime.parse("2023-10-01T12:00:00Z"));
    }

    @Test
    void shouldReturnCurrentUser() {
        String document = "{ me }";

        graphQlTester.mutate()
                .header("X-User-Id", "10086")
                .build()
                .document(document)
                .execute()
                .path("me")
                .entity(String.class)
                .isEqualTo("Current User ID: 10086");
    }

    @Test
    void shouldDenyUnauthenticatedUser() {
        String document = "{ me }";

        graphQlTester.document(document)
                .execute()
                .errors()
                .expect(error -> error.getMessage().contains("Access Denied") || error.getMessage().contains("Unauthorized"))
                .verify();
    }

    @Test
    void shouldDenyDeleteBookForNonAdmin() {
        String document = "mutation { deleteBook(id: 101) }";

        graphQlTester.mutate()
                .header("X-User-Id", "10086")
                .header("X-User-Roles", "ROLE_USER")
                .build()
                .document(document)
                .execute()
                .errors()
                .expect(error -> error.getMessage().contains("Access Denied"))
                .verify();
    }

    @Test
    void shouldAllowDeleteBookForAdmin() {
        String document = "mutation { deleteBook(id: 101) }";

        graphQlTester.mutate()
                .header("X-User-Id", "999")
                .header("X-User-Roles", "ROLE_ADMIN")
                .build()
                .document(document)
                .execute()
                .path("deleteBook")
                .entity(Boolean.class)
                .isEqualTo(true);
    }
}
