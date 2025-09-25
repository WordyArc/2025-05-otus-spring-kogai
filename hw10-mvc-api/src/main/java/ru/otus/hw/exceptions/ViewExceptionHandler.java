package ru.otus.hw.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice(basePackages = "ru.otus.hw.controllers.view")
public class ViewExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String handleTypeMismatch(Model model) {
        model.addAttribute("status", 400);
        model.addAttribute("message", "Invalid parameter");
        return "error";
    }

}
