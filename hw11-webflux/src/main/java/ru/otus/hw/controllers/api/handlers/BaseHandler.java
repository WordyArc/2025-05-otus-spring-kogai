package ru.otus.hw.controllers.api.handlers;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON;

abstract class BaseHandler {

    protected final Validator validator;

    protected BaseHandler(Validator validator) {
        this.validator = validator;
    }

    protected <T> Mono<ServerResponse> badRequestFromViolations(Set<ConstraintViolation<T>> violations) {
        var problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Bad Request");
        var detail = violations.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));
        problemDetail.setDetail(detail.isEmpty() ? "Validation error" : detail);
        return ServerResponse.badRequest().contentType(APPLICATION_JSON).bodyValue(problemDetail);
    }

    protected Mono<ServerResponse> badRequest(String detail) {
        var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Bad Request");
        pd.setDetail(detail);
        return ServerResponse.badRequest().contentType(APPLICATION_JSON).bodyValue(pd);
    }

    protected Mono<ServerResponse> notFound(String detail) {
        var pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setTitle("Not Found");
        pd.setDetail(detail);
        return ServerResponse.status(HttpStatus.NOT_FOUND).contentType(APPLICATION_JSON).bodyValue(pd);
    }

    protected Mono<ServerResponse> created(URI location, Object body) {
        return ServerResponse.created(location).contentType(APPLICATION_JSON).bodyValue(body);
    }

}
