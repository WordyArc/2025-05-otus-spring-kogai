package ru.otus.hw.exceptions;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Configuration
public class ErrorHandling {

    @Bean
    public HandlerFilterFunction<ServerResponse, ServerResponse> errorFilter() {
        return (request, next) -> next.handle(request)
                .onErrorResume(ex -> {
                    log.debug("Handling error", ex);
                    if (ex instanceof EntityNotFoundException enf) {
                        return error(HttpStatus.NOT_FOUND, "Not Found", enf.getMessage());
                    }
                    if (ex instanceof IllegalArgumentException iae) {
                        return error(HttpStatus.BAD_REQUEST, "Bad Request", iae.getMessage());
                    }
                    if (ex instanceof ConstraintViolationException cve) {
                        return error(HttpStatus.BAD_REQUEST, "Bad Request", cve.getMessage());
                    }
                    if (ex instanceof DecodingException) {
                        return error(HttpStatus.BAD_REQUEST, "Bad Request", "Malformed JSON");
                    }
                    if (ex instanceof ServerWebInputException swe) {
                        var cause = swe.getCause();
                        var detail = (cause instanceof DecodingException) ? "Malformed JSON" : "Invalid parameter";
                        return error(HttpStatus.BAD_REQUEST, "Bad Request", detail);
                    }
                    return error(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Unexpected error");
                });
    }

    private Mono<ServerResponse> error(HttpStatus status, String title, String detail) {
        var problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setTitle(title);
        problemDetail.setDetail(detail);
        return ServerResponse.status(status).contentType(APPLICATION_JSON).bodyValue(problemDetail);
    }
}
