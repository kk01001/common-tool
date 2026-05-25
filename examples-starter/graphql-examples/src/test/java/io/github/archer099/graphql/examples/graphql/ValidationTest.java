package io.github.archer099.graphql.examples.graphql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ValidationTest {

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
    void shouldFailValidationWhenTitleIsBlank() {
        String mutation = """
            mutation {
                createBook(input: {
                    id: "888",
                    title: "",
                    authorId: "1"
                }) {
                    id
                }
            }
        """;

        graphQlTester.document(mutation)
                .execute()
                .errors()
                .expect(error -> error.getMessage().contains("must not be blank") || error.getMessage().contains("不能为空"));
    }
}
