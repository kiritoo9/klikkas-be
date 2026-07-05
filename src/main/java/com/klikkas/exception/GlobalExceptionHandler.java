package com.klikkas.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.klikkas.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Not Found handler
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(
            NotFoundException ex) {
        return new ErrorResponse(
                ex.getMessage(),
                ex.getError());
    }

    // Bad request handler
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(BadRequestException ex) {
        return new ErrorResponse(
                ex.getMessage(), 
                "BAD_REQUEST_ERR"
        );
    }

    // Request body validation handler
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));

        return Map.of(
                "message", "Validation failed",
                "errors", errors);

    }

    // Route not found handler
    @ExceptionHandler(org.springframework.web.servlet.NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleRouteNotFound(
            org.springframework.web.servlet.NoHandlerFoundException ex) {
        return new ErrorResponse(
                "Route not found",
                "ROUTE_NOT_FOUND");
    }

}