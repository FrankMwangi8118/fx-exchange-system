package com.AnvilShieldGroup.rate_service.infrastructure.scheduler;

import com.AnvilShieldGroup.rate_service.infrastructure.cache.CacheService;
import com.AnvilShieldGroup.rate_service.infrastructure.external.ExchangeRateClient;
import com.AnvilShieldGroup.rate_service.infrastructure.external.dto.ExternalExchangeRateResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Array;
import java.util.*;

@Slf4j
@Service
public class SchedulerServiceImpl implements SchedulerService {

    private final ExchangeRateClient exchangeRateClient;
    private final CacheService cacheService;

    public SchedulerServiceImpl(ExchangeRateClient exchangeRateClient, CacheService cacheService) {
        this.exchangeRateClient = exchangeRateClient;
        this.cacheService = cacheService;
    }


    /**
     * a job that fetches rates of major/mostly exchanges/mostly traded currencies.
     * then caches them in caffeine to reduce api hits
     */
    @Override
    @Scheduled(fixedRateString = "${cache.entry.schedule-time}")
    public void preFetchMajorCurrenciesRates() {
        List<String> majorCurrencies = List.of("USD", "EUR", "JPY", "GBP", "CAD", "CHF", "AUD");

        Flux.fromIterable(majorCurrencies)
                .flatMap(currency -> exchangeRateClient.fetchExchangeRate(currency, null)
                        .doOnNext(response -> cacheService.putRate(currency, response.getData()))
                        .doOnError(e -> log.error("Error fetching rate for " + currency, e))
                        .onErrorResume(e -> Mono.empty()) // ignore failed calls
                )
                .blockLast(); // Wait for all to complete before method ends
    }


}
