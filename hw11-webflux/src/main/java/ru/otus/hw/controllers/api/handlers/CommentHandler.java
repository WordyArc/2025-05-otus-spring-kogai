package ru.otus.hw.controllers.api.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.mappers.CommentMapper;
import ru.otus.hw.services.CommentService;

import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;

@Component
@RequiredArgsConstructor
public class CommentHandler {
    private final CommentService commentService;

    public Mono<ServerResponse> listByBook(ServerRequest request) {
        long bookId = Long.parseLong(request.pathVariable("bookId"));
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromPublisher(
                        commentService.findAllByBookId(bookId).map(CommentMapper::toDto), CommentDto.class));
    }
}
