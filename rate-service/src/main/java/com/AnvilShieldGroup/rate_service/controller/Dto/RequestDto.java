package com.AnvilShieldGroup.rate_service.controller.Dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestDto {
    private String to;
    private String from;
}
