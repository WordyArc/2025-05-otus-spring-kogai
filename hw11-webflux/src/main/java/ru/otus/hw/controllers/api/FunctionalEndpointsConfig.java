package ru.otus.hw.controllers.api;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import ru.otus.hw.controllers.api.handlers.AuthorHandler;
import ru.otus.hw.controllers.api.handlers.BookHandler;
import ru.otus.hw.controllers.api.handlers.CommentHandler;
import ru.otus.hw.controllers.api.handlers.GenreHandler;
import ru.otus.hw.exceptions.ErrorHandling;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class FunctionalEndpointsConfig {

    @Bean
    RouterFunction<ServerResponse> composeRoutes(BookHandler bookHandler,
                                                 AuthorHandler authorHandler,
                                                 GenreHandler genreHandler,
                                                 CommentHandler commentHandler,
                                                 ErrorHandling errorHandling) {
        return route()
                // books
                .GET("/api/v1/books", accept(MediaType.APPLICATION_JSON), bookHandler::list)
                .GET("/api/v1/books/{id}", accept(MediaType.APPLICATION_JSON), bookHandler::get)
                .POST("/api/v1/books", accept(MediaType.APPLICATION_JSON).and(contentType(MediaType.APPLICATION_JSON)), bookHandler::create)
                .PUT("/api/v1/books/{id}", accept(MediaType.APPLICATION_JSON).and(contentType(MediaType.APPLICATION_JSON)), bookHandler::update)
                .PATCH("/api/v1/books/{id}", accept(MediaType.APPLICATION_JSON).and(contentType(MediaType.APPLICATION_JSON)), bookHandler::patch)
                .DELETE("/api/v1/books/{id}", accept(MediaType.APPLICATION_JSON), bookHandler::delete)

                // authors
                .GET("/api/v1/authors", accept(MediaType.APPLICATION_JSON), authorHandler::list)

                // genres
                .GET("/api/v1/genres", accept(MediaType.APPLICATION_JSON), genreHandler::list)

                // comments for book
                .GET("/api/v1/books/{bookId}/comments", accept(MediaType.APPLICATION_JSON), commentHandler::listByBook)
                .filter(errorHandling.errorFilter())
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> apiRoutes(BookHandler books,
                                                    AuthorHandler authors,
                                                    GenreHandler genres,
                                                    CommentHandler comments,
                                                    ErrorHandling errorHandling) {

        return route()
                .nest(path("/api/v1"), builder -> builder
                        .GET("/authors", authors::list)
                        .GET("/genres", genres::list)
                        .nest(path("/books"), b -> b
                                .GET("", books::list)
                                .POST("", books::create)
                                .GET("/{id}", books::get)
                                .PUT("/{id}", books::update)
                                .PATCH("/{id}", books::patch)
                                .DELETE("/{id}", books::delete)
                                .GET("/{id}/comments", comments::listByBook)
                        )
                )
                .filter(errorHandling.errorFilter())
                .build();
    }

}
