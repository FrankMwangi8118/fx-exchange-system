package com.AnvilShieldGroup.rate_service.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder

// a record is optional but i din,t see the use of the toString method

public class CustomExceptionDto{
    private final String responseMessage;
    private final Integer responseCode;
    private final String path;

}
