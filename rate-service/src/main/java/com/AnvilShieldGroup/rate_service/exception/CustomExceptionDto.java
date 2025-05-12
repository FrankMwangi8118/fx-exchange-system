package com.AnvilShieldGroup.rate_service.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder

// a record is optional
public class CustomExceptionDto{
    private final String responseMessage;
    private final Integer responseCode;
    private final String path;

}
