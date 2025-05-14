package com.AnvilShieldGroup.main_service.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;

import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({
            java.util.concurrent.TimeoutException.class,
            io.netty.handler.timeout.ReadTimeoutException.class
    })
    public ResponseEntity<CustomExceptionDto> handleTimeoutExceptions(Exception ex, ServerHttpRequest request) {
        log.warn("external server timed out");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                CustomExceptionDto.builder()
                        .responseCode(HttpStatus.SERVICE_UNAVAILABLE.value())
                        .responseMessage("External service timed out")
                        .path(request.getURI().getPath())
                        .build()
        );
    }

    //similiar to java.net  handles dns issues ie net issues
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
                        .path(request.getURI().getPath())
                        .build()
        );
    }

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

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<CustomExceptionDto> handleWebExchangeBindException(WebExchangeBindException ex,
                                                                             ServerHttpRequest request) {
        log.warn("validation failed for request with request id :{}",request.getId());
        String errorMessages = ex.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
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
        log.warn("a runtime exception occurred:{}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                CustomExceptionDto.builder()
                        .responseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .responseMessage("An unexpected error occurred: ")
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
