package com.AnvilShieldGroup.main_service.infrastructure.external.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExternalApiResponse{
    private Integer responseCode;
    private String responseStatus;
    private String responseMessage;
    private Data data;
}
