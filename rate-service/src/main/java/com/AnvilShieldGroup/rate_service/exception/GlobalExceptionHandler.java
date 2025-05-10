package com.AnvilShieldGroup.rate_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler extends RuntimeException {

    // Handle timeouts
    @ExceptionHandler({
            java.util.concurrent.TimeoutException.class,
            io.netty.handler.timeout.ReadTimeoutException.class
    })
    public ResponseEntity<CustomExceptionDto> handleTimeoutExceptions(Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                CustomExceptionDto.builder()
                        .responseCode(HttpStatus.SERVICE_UNAVAILABLE.value())
                        .responseMessage("External service timed out")
                        .path(request.getRequestURI())
                        .build()
        );
    }

    // Handle unknown host (DNS issues)
    @ExceptionHandler(java.net.UnknownHostException.class)
    public ResponseEntity<CustomExceptionDto> handleUnknownHost(Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                CustomExceptionDto.builder()
                        .responseCode(HttpStatus.SERVICE_UNAVAILABLE.value())
                        .responseMessage("External service is unreachable: Unknown host")
                        .path(request.getRequestURI())
                        .build()
        );
    }

    // Handle connection failures (e.g. refused connection)
    @ExceptionHandler(java.net.ConnectException.class)
    public ResponseEntity<CustomExceptionDto> handleConnectException(Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                CustomExceptionDto.builder()
                        .responseCode(HttpStatus.SERVICE_UNAVAILABLE.value())
                        .responseMessage("External service is unreachable: Connection failed")
                        .path(request.getRequestURI())
                        .build()
        );
    }

    //handles webclient exception ie 401,404,422
    //main   unprocessable entity
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<CustomExceptionDto> handleWebClientResponseException(WebClientResponseException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        return ResponseEntity.status(status != null ? status : HttpStatus.BAD_GATEWAY).body(
                CustomExceptionDto.builder()
                        .responseCode(ex.getStatusCode().value())
                        .responseMessage("WebClient error: ")
                        .path(request.getRequestURI())
                        .build()
        );
    }
    //handle validation errors
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CustomExceptionDto> handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        String errorMessages = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(" | "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                CustomExceptionDto.builder()
                        .responseCode(HttpStatus.BAD_REQUEST.value())
                        .responseMessage(errorMessages)
                        .path(request.getRequestURI())
                        .build()
        );
    }

    }

