package com.webshop.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ProblemDetail handleProductNotFound(ProductNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Termék nem található");
        problem.setType(URI.create("https://api.webshop.com/errors/not-found"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(OutOfStockException.class)
    public ProblemDetail handleOutOfStock(OutOfStockException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Készlethiány");
        problem.setType(URI.create("https://api.webshop.com/errors/out-of-stock"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleOutOfStock(EmailAlreadyExistsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Foglalt email");
        problem.setType(URI.create("https://api.webshop.com/errors/already-used-email"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}