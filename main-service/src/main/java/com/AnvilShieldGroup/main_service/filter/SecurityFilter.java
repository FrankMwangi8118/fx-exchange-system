package com.AnvilShieldGroup.main_service.filter;

import com.AnvilShieldGroup.main_service.controller.dto.ApiResponse;
import com.AnvilShieldGroup.main_service.exception.CustomExceptionDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * SecurityFilter is a WebFilter that intercepts requests to a specific endpoint,
 * checks for API key authentication, and either allows the request to proceed or
 * blocks it with an error response.
 */
@Slf4j
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 110) // Set order of execution in the filter chain
public class SecurityFilter implements WebFilter {

    // The endpoint that this filter protects
    private static final String TARGET_RATE_ENDPOINT = "/api/v1/mainService/convert";

    // Header names for authentication
    private static final String API_KEY_HEADER = "api-key";
    private static final String API_PASSPHRASE_HEADER = "api-passphrase";

    private static final String REQUEST_ID_MDC_KEY = "requestId";

    @Value("${api-key}")
    private String expectedApiKey ;
    @Value("${api-passphrase}")
    private String expectedApiPassphrase;

    // ObjectMapper is used to convert Java objects to JSON
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * This method intercepts HTTP requests, checks if they match the protected path,
     * and verifies the API key and passphrase.
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath(); // Get request path

        // Check if the request path matches the protected endpoint
        if (TARGET_RATE_ENDPOINT.equals(path)) {
            // Extract API credentials from request headers
            String apiKey = request.getHeaders().getFirst(API_KEY_HEADER);
            String apiPassphrase = request.getHeaders().getFirst(API_PASSPHRASE_HEADER);
            log.info("dat:{} :{}",apiPassphrase,apiKey);
            // Validate the credentials
            if (expectedApiKey.equals(apiKey) && expectedApiPassphrase.equals(apiPassphrase)) {
                log.info("Request ID: {} - API Key Auth SUCCESS for {}", MDC.get(REQUEST_ID_MDC_KEY), path);
                // Continue with the filter chain if authentication passes
                return chain.filter(exchange);
            } else {
                log.warn("Request ID: {} - API Key Auth FAILED for {}", MDC.get(REQUEST_ID_MDC_KEY), path);

                // Create error response body
                CustomExceptionDto dto = CustomExceptionDto.builder()
                        .responseCode(HttpStatus.UNAUTHORIZED.value())
                        .responseMessage("Invalid API security credentials")
                        .path(path)
                        .build();
                ApiResponse apiResponse= ApiResponse.builder()
                        .responseCode(HttpStatus.UNAUTHORIZED.value())
                        .responseMessage("invalid security credentials")
                        .data(dto)
                        .build();

                byte[] jsonBytes;
                try {
                    // Convert the DTO to JSON bytes
                    jsonBytes = objectMapper.writeValueAsBytes(apiResponse);
                } catch (Exception e) {
                    // Fallback in case serialization fails
                    jsonBytes = ("{\"responseMessage\":\"Serialization error\"}")
                            .getBytes(StandardCharsets.UTF_8);
                }
                // Set HTTP response headers and status code
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

                // Write the JSON error to the response body so as to figure out mainly you will get a json serialization error
                return exchange.getResponse().writeWith(Mono.just(
                        exchange.getResponse().bufferFactory().wrap(jsonBytes)
                ));
            }
        }

        // If the path is not protected, continue the chain without filtering
        return chain.filter(exchange);
    }
}