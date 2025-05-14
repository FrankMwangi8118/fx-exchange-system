package com.AnvilShieldGroup.main_service.controller.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestDto {
    @NotNull(message = "'from' cannot be null")
    @Size(min = 3, max = 3, message = "from must be exactly 3 letters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "'from' must contain exactly three uppercase letters as per ISO 4217")
    private String from;

    @NotNull(message = "'from' cannot be null")
    @Size(min = 3, max = 3, message = "'to' must be exactly 3 letters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "'to' must contain exactly three uppercase letters as per ISO 4217")
    private String to;

    @NotNull(message = "'amount' cannot be null")
    @DecimalMin(value = "0.00", inclusive = false, message = "Amount must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Amount must be a valid monetary value (up to 2 decimal places)")
    private BigDecimal amount;
}
