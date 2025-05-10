package com.AnvilShieldGroup.rate_service.controller.Dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestDto {
    @Size(min = 3,max = 3,message = "'to' must be exactly 3 letters")
    @Pattern(regexp ="^[A-Z]{3}$", message = " 'to' must contain exactly three uppercase letters" )
    private String to;

    @NotNull(message = "'from' cannot be null")
    @Size(min = 3,max = 3,message = "'from' must be exactly 3 letters")
    @Pattern(regexp ="^[A-Z]{3}$", message = " 'from' must contain exactly three uppercase letters" )
    private String from;
}
