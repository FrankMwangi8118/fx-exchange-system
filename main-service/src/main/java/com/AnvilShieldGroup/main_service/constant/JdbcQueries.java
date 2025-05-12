package com.AnvilShieldGroup.main_service.constant;
public class JdbcQueries {
    public static final String PERSIST_QUERY =
            "INSERT INTO conversions (conversion_id, from_currency, to_currency, rate, amount, converted_amount) VALUES (?, ?, ?, ?, ?, ?)";
}
