package com.AnvilShieldGroup.rate_service.infrastructure.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class CacheServiceImpl implements CacheService {
    // cache structure: key: base currency (eg"USD"),value Map(target/to currency,rate)
    //"USD",<"CAD":1.45333>

    private final Cache<String, Map<String, Double>> cache;

    public CacheServiceImpl(Cache<String, Map<String, Double>> cache) {
        this.cache = cache;
    }


    /**
     * Attempts to retrieve a cached exchange rate from the 'from' to 'to' currency.
     *
     * @param from base currency (e.g., "USD")
     * @param to   target currency (e.g., "CAD")
     * @return the exchange rate if present, otherwise null
     */
    @Override
    public Double getRateFromCache(String from, String to) {
        Map<String, Double> rates = cache.getIfPresent(from);
        if (rates != null) {
            log.info("cache hit for:{}:{}", to, rates.get(to));
            return rates.get(to);
        }
        return null;
    }

    /**
     * Puts new rates in the cache for a given base currency.
     * If there's already a map for that base currency, it merges the values.
     *
     * @param from base currency (e.g., "USD")
     * @param newRates map of target currency → rate (e.g., { "CAD": 143.0, "EUR": 0.91 })
     */
    @Override
    public void putRate(String from, Map<String, Double> newRates) {
        Map<String, Double> existingRates = cache.getIfPresent(from);
        if (existingRates == null) {
            cache.put(from, newRates);
            log.info("cache put for currency :{} rates:{}",from,newRates);
        } else {

            existingRates.putAll(newRates);
            cache.put(from, existingRates); //re-put the merged map
            log.info("cache rePut for currency :{} rates:{}",from,newRates);

        }
    }

}
