package com.AnvilShieldGroup.main_service.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
public class CustomExceptionDto {
    private int responseCode;
    private String responseMessage;
    private String path;
}
