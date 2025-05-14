package com.AnvilShieldGroup.main_service.infrastructure.external.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FetchRateResponse {
    private Integer responseCode;
    private String responseStatus;
    private String responseMessage;
    private Data data;
}
