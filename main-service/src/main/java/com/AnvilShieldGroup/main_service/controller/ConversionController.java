package com.AnvilShieldGroup.main_service.controller;

import com.AnvilShieldGroup.main_service.controller.dto.ApiResponse;
import com.AnvilShieldGroup.main_service.controller.dto.RequestDto;
import com.AnvilShieldGroup.main_service.service.ConversionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
@Slf4j
@RestController
@RequestMapping("/api/v1/mainService")
@AllArgsConstructor
@Tag(name = "Currency Conversion API", description = "Converts currency values")
public class ConversionController {
    private final ConversionService conversionService;
    @Operation(summary = "Convert currency", description = "Converts amount from one currency to another")
    @PostMapping("/convert")
    public Mono<ResponseEntity<ApiResponse>> convert(@RequestBody @Valid RequestDto requestDto) {
log.info("conversion request ... :{} from :{} amount :{} at :{}",requestDto.getFrom(),requestDto.getTo(),requestDto.getAmount(), LocalDateTime.now());
        return conversionService.convertCurrency(requestDto)
                .map(responseDto -> ResponseEntity.ok(
                        ApiResponse.builder()
                                .responseCode(200)
                                .responseStatus("success")
                                .data(responseDto)
                                .build()
                ));
    }

    @GetMapping("/status")
    @Operation(
            summary = "gets the status of the service",
            description = "checks the availability of the server"
    )
    public ResponseEntity<?> status() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "up");
        return ResponseEntity.ok().body(status);
    }
}
