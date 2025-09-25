package ru.otus.hw.exceptions;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.NoSuchElementException;

@ControllerAdvice(basePackages = "ru.otus.hw.controllers.view")
public class ViewExceptionHandler {

    @ExceptionHandler({EntityNotFoundException.class, NoSuchElementException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(Model model) {
        model.addAttribute("status", 404);
        model.addAttribute("message", "Not Found");
        return "error";
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidation(Model model) {
        model.addAttribute("status", 400);
        model.addAttribute("message", "Validation error");
        return "error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneric(Model model) {
        model.addAttribute("status", 500);
        model.addAttribute("message", "Unexpected error");
        return "error";
    }
}
