package ru.otus.hw.controllers.api;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import ru.otus.hw.controllers.api.handlers.AuthorHandler;
import ru.otus.hw.controllers.api.handlers.BookHandler;
import ru.otus.hw.controllers.api.handlers.CommentHandler;
import ru.otus.hw.controllers.api.handlers.GenreHandler;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class FunctionalEndpointsConfig {

    @Bean
    public RouterFunction<ServerResponse> composedRoutes(
            BookHandler books,
            AuthorHandler authors,
            GenreHandler genres,
            CommentHandler comments,
            HandlerFilterFunction<ServerResponse, ServerResponse> errorFilter) {

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
                .filter(errorFilter)
                .build();
    }

}
