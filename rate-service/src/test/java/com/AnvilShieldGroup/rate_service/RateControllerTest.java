package com.AnvilShieldGroup.rate_service;

import com.AnvilShieldGroup.rate_service.controller.ConversionController; // Import the actual controller
import com.AnvilShieldGroup.rate_service.controller.Dto.RequestDto;
import com.AnvilShieldGroup.rate_service.controller.Dto.ResponseDto;
import com.AnvilShieldGroup.rate_service.controller.response.ApiResponse;
import com.AnvilShieldGroup.rate_service.exception.CustomExceptionDto; // Import your DTO
import com.AnvilShieldGroup.rate_service.exception.GlobalExceptionHandler; // Import your handler
import com.AnvilShieldGroup.rate_service.infrastructure.external.ExchangeRateClient;
import com.AnvilShieldGroup.rate_service.service.ExchangeRateService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import; // Import your GlobalExceptionHandler
import org.springframework.core.ParameterizedTypeReference; // Needed for generic types
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@WebFluxTest(ConversionController.class)
@Import(GlobalExceptionHandler.class)
public class RateControllerTest {
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private Validator validator;

    @Autowired
    private ExchangeRateService currencyConversionService;

    @Autowired
    private ExchangeRateClient exchangeRateClient;

    private ResponseDto mockResponseDto;

    @TestConfiguration
    static class TestConfig {


        @Bean
        public Validator testValidator() {
            return mock(Validator.class);
        }

        @Bean
        public ExchangeRateService testCurrencyConversionService() {
            return mock(ExchangeRateService.class);
        }

        @Bean
        public ExchangeRateClient testExchangeRateClient() {
            return mock(ExchangeRateClient.class);
        }
    }

    @BeforeEach
    void setUp() {
        // Set up a mock ResponseDto for successful scenarios
        mockResponseDto = ResponseDto.builder()
                .from("USD")
                .to("EUR")
                .rate(0.85) //todo change to big decimal
                .build();
    }

    @Test
    void convert_ValidRequest_ReturnsSuccessResponse() {
        when(validator.validate(any(RequestDto.class))).thenReturn(Collections.emptySet());
        when(currencyConversionService.getCurrencyQuote(any(RequestDto.class)))
                .thenReturn(Mono.just(mockResponseDto));

        webTestClient.get().uri("/api/v1/rateService/rate?from=USD&to=EUR")
                .accept(MediaType.APPLICATION_JSON)
                .exchange() // Perform the request
                .expectStatus().isOk() // Expect HTTP 200 OK
                .expectBody(new ParameterizedTypeReference<ApiResponse<ResponseDto>>() {
                })
                .consumeWith(response -> {
                    //  response body structure and content
                    ApiResponse<ResponseDto> apiResponse = response.getResponseBody();
                    assertNotNull(apiResponse, "Response body should not be null");
                    assertEquals("success", apiResponse.getResponseStatus(), "Response status should be 'success'");
                    assertNotNull(apiResponse.getData(), "Data payload should not be null");

                    ResponseDto responseData = apiResponse.getData();
                    assertEquals("USD", responseData.getFrom(), "From currency should match");
                    assertEquals("EUR", responseData.getTo(), "To currency should match");
                    //todo: change to decimal
                    assertEquals(0, responseData.getRate().compareTo(0.85), "Rate should match numerically");
                });
    }

    @Test
    void convert_InvalidRequest_ReturnsBadRequestDueToValidation() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<RequestDto> violation = mock(ConstraintViolation.class);

        jakarta.validation.Path mockPath = mock(jakarta.validation.Path.class);
        when(mockPath.toString()).thenReturn("from");
        when(violation.getPropertyPath()).thenReturn(mockPath);
        when(violation.getMessage()).thenReturn("cannot be blank");
        Set<ConstraintViolation<RequestDto>> violations = new HashSet<>();
        violations.add(violation);
        when(validator.validate(any(RequestDto.class))).thenReturn(violations);
        webTestClient.get().uri("/api/v1/rateService/rate?from=&to=EUR") // Use invalid data to trigger validation
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest() // Expect HTTP 400 Bad Request
                .expectBody(CustomExceptionDto.class) // Expect your custom DTO in the body
                .consumeWith(response -> {
                    CustomExceptionDto errorBody = response.getResponseBody();
                    assertNotNull(errorBody, "Error response body should not be null");
                    assertEquals(HttpStatus.BAD_REQUEST.value(), errorBody.getResponseCode(), "Response code should be 400");
                    assertEquals("from: cannot be blank", errorBody.getResponseMessage(), "Error message should match validation output format");
                    assertEquals("/api/v1/rateService/rate", errorBody.getPath(), "Path should match the request URI");
                });

        verify(currencyConversionService, never()).getCurrencyQuote(any(RequestDto.class));
    }


}