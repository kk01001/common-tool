package io.github.archer099.graphql.examples.graphql;

import io.github.archer099.graphql.examples.dto.CreateBookInput;
import io.github.archer099.graphql.examples.model.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.graphql.test.tester.WebSocketGraphQlTester;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SubscriptionTest {

    @LocalServerPort
    private int port;

    private WebSocketGraphQlTester graphQlTester;

    @BeforeEach
    void setUp() {
        URI url = URI.create("ws://localhost:" + port + "/graphql");
        WebSocketClient client = new ReactorNettyWebSocketClient();
        this.graphQlTester = WebSocketGraphQlTester.builder(url, client).build();
    }

    @Test
    void shouldReceiveBookCreatedSubscription() {
        Flux<Map> bookFlux = graphQlTester.document("subscription { bookCreated { id title author { name } publishDate } }")
                .executeSubscription()
                .toFlux("bookCreated", Map.class);

        // Start the subscription verification
        StepVerifier.create(bookFlux)
                .then(() -> {
                    // Trigger mutation to create a book
                    String mutation = """
                        mutation {
                            createBook(input: {
                                id: "999",
                                title: "Reactive Spring",
                                authorId: "1",
                                publishDate: "2023-10-01T12:00:00Z"
                            }) {
                                id
                            }
                        }
                    """;
                    
                    // We need a separate execution for mutation. 
                    // Since WebSocketGraphQlTester is multiplexed, we can use it for mutation too, 
                    // or we can use a separate HTTP client. 
                    // Using the same tester is fine if the server supports operations over WebSocket.
                    // Spring GraphQL supports queries/mutations over WebSocket.
                    graphQlTester.document(mutation).execute().errors().verify();
                })
                .expectNextMatches(book -> 
                    book.get("id").equals("999") && 
                    book.get("title").equals("Reactive Spring") &&
                    book.get("author") != null
                )
                .thenCancel()
                .verify();
    }
}
