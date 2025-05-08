package com.AnvilShieldGroup.rate_service.controller;

import com.AnvilShieldGroup.rate_service.controller.Dto.ResponseDto;
import com.AnvilShieldGroup.rate_service.controller.response.ApiResponse;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rateService/")
public class ConversionController {
    @GetMapping
    public ResponseEntity<ApiResponse<ResponseDto>> convert() {
        return new ResponseEntity<>(HttpStatusCode.valueOf(200));
    }

}
