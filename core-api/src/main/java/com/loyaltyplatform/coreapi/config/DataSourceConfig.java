package com.loyaltyplatform.coreapi.config;

import com.loyaltyplatform.coreapi.tenant.TenantAwareDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Registering our own DataSource bean here makes Spring Boot's
 * auto-configured one back off (it's @ConditionalOnMissingBean), so every
 * connection the app hands out — for any repository or service — passes
 * through TenantAwareDataSource first.
 */
@Configuration
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource dataSource(DataSourceProperties properties) {
        HikariDataSource hikariDataSource =
                properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
        return new TenantAwareDataSource(hikariDataSource);
    }
}
