package ru.otus.hw.controllers.api.handlers;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.codec.DecodingException;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.SmartValidator;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookFormDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.mappers.BookMapper;
import ru.otus.hw.services.BookService;
import ru.otus.hw.util.PatchUtils;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;
import static org.springframework.web.reactive.function.server.ServerResponse.created;
import static org.springframework.web.reactive.function.server.ServerResponse.noContent;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Component
@RequiredArgsConstructor
public class BookHandler {

    private final BookService bookService;
    private final Validator validator;

    private static final ParameterizedTypeReference<Map<String, Object>> MAP =
            new ParameterizedTypeReference<>() {};

    public Mono<ServerResponse> list(ServerRequest req) {
        return ok().contentType(APPLICATION_JSON)
                .body(fromPublisher(bookService.findAll().map(BookMapper::toDto), BookDto.class));
    }

    public Mono<ServerResponse> get(ServerRequest req) {
        long id = parseId(req);
        return bookService.getById(id)
                .map(BookMapper::toDto)
                .flatMap(dto -> ok().contentType(APPLICATION_JSON).bodyValue(dto));
    }

    public Mono<ServerResponse> create(ServerRequest req) {
        return req.bodyToMono(BookFormDto.class)
                .flatMap(this::validate)
                .flatMap(f -> bookService.insert(f.title(), f.authorId(), f.genreIds()))
                .map(BookMapper::toDto)
                .flatMap(dto -> created(location(req, dto.id()))
                        .contentType(APPLICATION_JSON)
                        .bodyValue(dto));
    }

    public Mono<ServerResponse> update(ServerRequest req) {
        long id = parseId(req);
        return req.bodyToMono(BookFormDto.class)
                .flatMap(this::validate)
                .flatMap(f -> bookService.update(id, f.title(), f.authorId(), f.genreIds()))
                .map(BookMapper::toDto)
                .flatMap(dto -> ok().contentType(APPLICATION_JSON).bodyValue(dto));
    }

    public Mono<ServerResponse> patch(ServerRequest req) {
        long id = parseId(req);
        return req.bodyToMono(MAP)
                .flatMap(patch -> bookService.getById(id)
                        .map(existing -> PatchUtils.mergeBookPatch(patch, existing)))
                .flatMap(args -> bookService.update(id, args.title(), args.authorId(), args.genreIds()))
                .map(BookMapper::toDto)
                .flatMap(dto -> ok().contentType(APPLICATION_JSON).bodyValue(dto));
    }

    public Mono<ServerResponse> delete(ServerRequest req) {
        long id = parseId(req);
        return bookService.deleteById(id).then(noContent().build());
    }

    private long parseId(ServerRequest req) {
        return Long.parseLong(req.pathVariable("id"));
    }

    private Mono<BookFormDto> validate(BookFormDto dto) {
        var violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        return Mono.just(dto);
    }

    private static URI location(ServerRequest req, Long id) {
        return req.uriBuilder().path("/api/v1/books/{id}").build(id);
    }
}