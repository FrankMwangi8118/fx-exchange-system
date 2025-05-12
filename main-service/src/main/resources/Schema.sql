-- todo Create the conversions table if it doesn't exist,add indexing for query performance
-- according to ISO 4217 (***) currency codes are 3 upper-case letters

CREATE TABLE IF NOT EXISTS conversions (
    conversion_id        VARCHAR PRIMARY KEY,
    from_currency        VARCHAR(3) NOT NULL,
    to_currency          VARCHAR(3) NOT NULL,
    rate                 NUMERIC NOT NULL CHECK (rate > 0),
    amount               NUMERIC NOT NULL CHECK (amount >= 0),
    converted_amount     NUMERIC NOT NULL CHECK (converted_amount >= 0),
    requested_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_conversions_from_to
    ON conversions (from_currency, to_currency);
CREATE INDEX IF NOT EXISTS idx_conversions_requested_at
    ON conversions (requested_at);
