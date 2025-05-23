package com.AnvilShieldGroup.rate_service.exception;

// Import the WebFlux equivalent

import org.springframework.http.server.reactive.ServerHttpRequest;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;

import java.util.stream.Collectors;

@ControllerAdvice

public class GlobalExceptionHandler {

    // Handle timeouts
    @ExceptionHandler({
            java.util.concurrent.TimeoutException.class,
            io.netty.handler.timeout.ReadTimeoutException.class
    })
    public ResponseEntity<CustomExceptionDto> handleTimeoutExceptions(Exception ex, ServerHttpRequest request) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                CustomExceptionDto.builder()
                        .responseCode(HttpStatus.SERVICE_UNAVAILABLE.value())
                        .responseMessage("External service timed out")
                        .path(request.getURI().getPath())
                        .build()
        );
    }

    // Handle unknown host (DNS issues)/network issues
    @ExceptionHandler(java.net.UnknownHostException.class)
    public ResponseEntity<CustomExceptionDto> handleUnknownHost(Exception ex, ServerHttpRequest request) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                CustomExceptionDto.builder()
                        .responseCode(HttpStatus.SERVICE_UNAVAILABLE.value())
                        .responseMessage("External service is unreachable: Unknown host")
                        // *** Get path from ServerHttpRequest ***
                        .path(request.getURI().getPath())
                        .build()
        );
    }

    @ExceptionHandler(java.net.ConnectException.class)
    public ResponseEntity<CustomExceptionDto> handleConnectException(Exception ex, ServerHttpRequest request) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                CustomExceptionDto.builder()
                        .responseCode(HttpStatus.SERVICE_UNAVAILABLE.value())
                        .responseMessage("External service is unreachable: Connection failed")
                        // *** Get path from ServerHttpRequest ***
                        .path(request.getURI().getPath())
                        .build()
        );
    }

    // handles webclient exception ie 401,404,422
    // main   unprocessable entity
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<CustomExceptionDto> handleWebClientResponseException(WebClientResponseException ex, ServerHttpRequest request) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        return ResponseEntity.status(status != null ? status : HttpStatus.BAD_GATEWAY).body(
                CustomExceptionDto.builder()
                        .responseCode(ex.getStatusCode().value())
                        .responseMessage("WebClient error: " + ex.getMessage())
                        .path(request.getURI().getPath())
                        .build()
        );
    }

    //handle validation errors
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CustomExceptionDto> handleConstraintViolationException(
            ConstraintViolationException ex,
            ServerHttpRequest request) {

        String errorMessages = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(" | "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                CustomExceptionDto.builder()
                        .responseCode(HttpStatus.BAD_REQUEST.value())
                        .responseMessage(errorMessages)
                        .path(request.getURI().getPath())
                        .build()
        );
    }


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<CustomExceptionDto> handleRuntimeException(RuntimeException ex, ServerHttpRequest request) {
        // Log the error
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                CustomExceptionDto.builder()
                        .responseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .responseMessage("An unexpected error occurred: " + ex.getMessage())
                        .path(request.getURI().getPath())
                        .build()
        );
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<CustomExceptionDto> handleSecurityException(SecurityException securityException, ServerWebExchange exchange) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value()).body(
                CustomExceptionDto.builder()
                        .responseCode(HttpStatus.UNAUTHORIZED.value())
                        .responseMessage("invalid api-security credentials")
                        .path(exchange.getRequest().getURI().getPath())
                        .build()
        );
    }
}