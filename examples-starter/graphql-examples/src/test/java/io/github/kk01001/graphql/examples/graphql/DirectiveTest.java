package io.github.kk01001.graphql.examples.graphql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DirectiveTest {

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
    void shouldReturnUppercaseFormattedTitle() {
        String query = """
            query {
                books(page: 1, size: 1) {
                    content {
                        title
                        formattedTitle
                    }
                }
            }
        """;

        graphQlTester.document(query)
                .execute()
                .path("books.content[0].formattedTitle")
                .entity(String.class)
                .satisfies(formattedTitle -> {
                    assertThat(formattedTitle).isNotNull();
                    assertThat(formattedTitle).isEqualTo(formattedTitle.toUpperCase());
                });
    }
}
