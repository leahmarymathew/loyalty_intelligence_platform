-- V2 used CHAR(3), but the JPA entity maps `currency` as a plain String
-- column and Hibernate's schema validator expects VARCHAR for that mapping.
-- Fixed forward with a new migration rather than editing V2, which may
-- already have been applied.
ALTER TABLE transactions ALTER COLUMN currency TYPE VARCHAR(3);
