package io.github.kk01001.graphql.examples.graphql;

import io.github.kk01001.graphql.examples.event.BookCreatedEvent;
import io.github.kk01001.graphql.examples.model.Book;
import org.reactivestreams.Publisher;
import org.springframework.context.event.EventListener;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Controller
public class SubscriptionController {

    private final Sinks.Many<Book> bookCreatedSink = Sinks.many().multicast().onBackpressureBuffer();

    @EventListener
    public void onBookCreated(BookCreatedEvent event) {
        bookCreatedSink.tryEmitNext(event.getBook());
    }

    @SubscriptionMapping
    public Publisher<Book> bookCreated() {
        return bookCreatedSink.asFlux();
    }
}
