package com.AnvilShieldGroup.main_service.service;

import com.AnvilShieldGroup.main_service.controller.dto.RequestDto;
import com.AnvilShieldGroup.main_service.controller.dto.ResponseDto;
import com.AnvilShieldGroup.main_service.infrastructure.Repository.ConversionRepositoryService;
import com.AnvilShieldGroup.main_service.infrastructure.external.FetchRateClient;
import com.AnvilShieldGroup.main_service.model.Conversion;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;

@Service
public class ConversionServiceImpl implements ConversionService {
    private final FetchRateClient fetchRateClient;
    private final ConversionRepositoryService conversionRepositoryService;

    public ConversionServiceImpl(FetchRateClient fetchRateClient, ConversionRepositoryService conversionRepositoryService) {
        this.fetchRateClient = fetchRateClient;
        this.conversionRepositoryService = conversionRepositoryService;
    }


    @Override
    public Mono<ResponseDto> convertCurrency(RequestDto requestDto) {
        return getRateFromExternal(requestDto.getFrom(), requestDto.getTo())
                .flatMap(rate ->
                        {
                            BigDecimal calculatedAmount = calculate(rate, requestDto.getAmount());
                            Conversion conversion = new Conversion();
                            conversion.setToCurrency(requestDto.getTo());
                            conversion.setFromCurrency(requestDto.getFrom());
                            conversion.setRate(rate);
                            conversion.setAmount(requestDto.getAmount());
                            conversion.setConvertedAmount(calculatedAmount);
                            return Mono.fromCallable(() -> conversionRepositoryService.save(conversion).toResponseDto())
                                    .subscribeOn(Schedulers.boundedElastic());
                        }
                );

    }

    private BigDecimal calculate(BigDecimal rate, BigDecimal amount) {
        return amount.multiply(rate);
    }

    @Override
    public Mono<BigDecimal> getRateFromExternal(String from, String to) {
        System.out.println("Calling external rate service...");
        return fetchRateClient
                .fetchRateFromRateService(from, to)
                .doOnNext(resp -> System.out.println("Received: " + resp))
                .map(response -> response.getData().getRate());
    }


}
