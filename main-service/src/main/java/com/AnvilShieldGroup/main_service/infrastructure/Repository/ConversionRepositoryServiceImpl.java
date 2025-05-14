package com.AnvilShieldGroup.main_service.infrastructure.Repository;

import com.AnvilShieldGroup.main_service.constant.JdbcQueries;
import com.AnvilShieldGroup.main_service.infrastructure.Repository.entity.ConversionsTbl;
import com.AnvilShieldGroup.main_service.model.Conversion;
import com.AnvilShieldGroup.main_service.util.IdGeneratorUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;

@Repository
public class ConversionRepositoryServiceImpl implements ConversionRepositoryService {
    private final IdGeneratorUtil idGeneratorUtil;
    private final JdbcTemplate jdbcTemplate;

    public ConversionRepositoryServiceImpl(IdGeneratorUtil idGeneratorUtil, JdbcTemplate jdbcTemplate) {
        this.idGeneratorUtil = idGeneratorUtil;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * @param conversion
     * @return
     */
    @Override
    public Conversion save(Conversion conversion) {

        ConversionsTbl conversionsTbl = ConversionsTbl.builder()
                .conversionId(idGeneratorUtil.idGenerator())
                .rate(conversion.getRate())
                .fromCurrency(conversion.getFromCurrency())
                .toCurrency(conversion.getToCurrency())
                .requestedAt(conversion.getRequestedAt())
                .convertedAmount(conversion.getConvertedAmount())
                .amount(conversion.getAmount())
                .requestedAt(Timestamp.from(Instant.now()))
                .build();
    int affectedRows=jdbcTemplate.update(
            JdbcQueries.PERSIST_QUERY,
            conversionsTbl.getConversionId(),
            conversionsTbl.getFromCurrency(),
            conversionsTbl.getToCurrency(),
            conversionsTbl.getRate(),
            conversionsTbl.getAmount(),
            conversionsTbl.getConvertedAmount()
    );

        return conversionsTbl.toConversion();
    }
}
