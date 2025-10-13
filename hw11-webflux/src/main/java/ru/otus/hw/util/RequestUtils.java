package ru.otus.hw.util;

import lombok.experimental.UtilityClass;
import org.springframework.web.reactive.function.server.ServerRequest;

@UtilityClass
public class RequestUtils {
    public long pathLong(ServerRequest req, String name) {
        try {
            return Long.parseLong(req.pathVariable(name));
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Invalid parameter", ex);
        }
    }
}
