package ru.otus.hw.controllers.api.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.mappers.GenreMapper;
import ru.otus.hw.services.GenreService;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
@RequiredArgsConstructor
public class GenreHandler {
    private final GenreService genreService;

    public Mono<ServerResponse> list(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(genreService.findAll()
                        .map(GenreMapper::toDto), GenreDto.class);
    }
}
