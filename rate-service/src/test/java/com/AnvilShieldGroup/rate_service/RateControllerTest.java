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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

// Import JUnit 5 Assertions for better readability
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue; // For checking message content

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Use @WebFluxTest to test the controller layer specifically
@WebFluxTest(ConversionController.class)
// Import the GlobalExceptionHandler so it's included in the test context
@Import(GlobalExceptionHandler.class)
public class RateControllerTest { // Renamed class as requested

    // WebTestClient is used to test Spring WebFlux controllers
    @Autowired
    private WebTestClient webTestClient;

    // Autowire the mock beans provided by the TestConfiguration
    // These will be the Mockito mocks we define below
    @Autowired
    private Validator validator;

    @Autowired
    private ExchangeRateService currencyConversionService;

    // Although injected, ExchangeRateClient is not used in the tested method,
    // but we still need to provide a mock for Spring to inject it.
    @Autowired
    private ExchangeRateClient exchangeRateClient;

    private ResponseDto mockResponseDto;

    // Define a TestConfiguration to provide mock beans to the context
    // This replaces @MockBean (which is deprecated)
    @TestConfiguration
    static class TestConfig {

        // Define mock beans using standard Mockito.mock()
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
        // Set up a standard mock ResponseDto for successful scenarios
        mockResponseDto = ResponseDto.builder()
                .from("USD")
                .to("EUR")
                .rate(0.85) // Use BigDecimal for rate
                .build();
    }

    @Test
    void convert_ValidRequest_ReturnsSuccessResponse() {
        // Arrange: Mock validator to return no violations (valid)
        when(validator.validate(any(RequestDto.class))).thenReturn(Collections.emptySet());

        // Arrange: Mock service to return a successful Mono with the mock response
        when(currencyConversionService.getCurrencyQuote(any(RequestDto.class)))
                .thenReturn(Mono.just(mockResponseDto));

        // Act & Assert: Perform the GET request and verify the response
        webTestClient.get().uri("/api/v1/rateService/rate?from=USD&to=EUR")
                .accept(MediaType.APPLICATION_JSON)
                .exchange() // Perform the request
                .expectStatus().isOk() // Expect HTTP 200 OK
                // Use ParameterizedTypeReference for generic types (ApiResponse<ResponseDto>)
                .expectBody(new ParameterizedTypeReference<ApiResponse<ResponseDto>>() {})
                .consumeWith(response -> {
                    // Further assertions on the response body structure and content
                    ApiResponse<ResponseDto> apiResponse = response.getResponseBody();
                    assertNotNull(apiResponse, "Response body should not be null");
                    assertEquals("success", apiResponse.getResponseStatus(), "Response status should be 'success'");
                    assertNotNull(apiResponse.getData(), "Data payload should not be null");

                    ResponseDto responseData = apiResponse.getData();
                    assertEquals("USD", responseData.getFrom(), "From currency should match");
                    assertEquals("EUR", responseData.getTo(), "To currency should match");
                    // Compare BigDecimal using compareTo() for numerical equality
                    assertEquals(0, responseData.getRate().compareTo(0.85), "Rate should match numerically");
                });
    }

    @Test
    void convert_InvalidRequest_ReturnsBadRequestDueToValidation() {
        // Arrange: Create a mock ConstraintViolation to simulate validation failure
        @SuppressWarnings("unchecked")
        ConstraintViolation<RequestDto> violation = mock(ConstraintViolation.class);

        // Stub getPropertyPath() and getMessage() on the mock violation
        // This is needed because your GlobalExceptionHandler uses these methods
        jakarta.validation.Path mockPath = mock(jakarta.validation.Path.class);
        when(mockPath.toString()).thenReturn("from"); // Simulate the property path "from"

        when(violation.getPropertyPath()).thenReturn(mockPath);
        when(violation.getMessage()).thenReturn("cannot be blank"); // Simulate a validation message

        Set<ConstraintViolation<RequestDto>> violations = new HashSet<>();
        violations.add(violation);

        // Arrange: Mock validator to return violations (invalid)
        // This will cause the controller to throw ConstraintViolationException
        when(validator.validate(any(RequestDto.class))).thenReturn(violations);

        // Act & Assert: Perform the GET request with invalid parameters (e.g., empty 'from')
        webTestClient.get().uri("/api/v1/rateService/rate?from=&to=EUR") // Use invalid data to trigger validation
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest() // Expect HTTP 400 Bad Request
                // Assert against the CustomExceptionDto structure returned by GlobalExceptionHandler
                .expectBody(CustomExceptionDto.class) // Expect your custom DTO in the body
                .consumeWith(response -> {
                    CustomExceptionDto errorBody = response.getResponseBody();
                    assertNotNull(errorBody, "Error response body should not be null");
                    assertEquals(HttpStatus.BAD_REQUEST.value(), errorBody.getResponseCode(), "Response code should be 400");
                    // Check the message format produced by your handler (e.g., "from: cannot be blank")
                    assertEquals("from: cannot be blank", errorBody.getResponseMessage(), "Error message should match validation output format");
                    assertEquals("/api/v1/rateService/rate", errorBody.getPath(), "Path should match the request URI");
                });

        // Verify that the service was NOT called because validation failed first
        verify(currencyConversionService, never()).getCurrencyQuote(any(RequestDto.class));
    }



}