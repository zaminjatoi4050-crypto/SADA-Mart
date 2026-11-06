package com.sadamart.infrastructure;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties(OracleDatabaseProperties.class)
@ConditionalOnProperty(prefix = "sadamart.oracle", name = "enabled", havingValue = "true")
public class OracleDataSourceConfiguration {
    @Bean(destroyMethod = "close")
    DataSource dataSource(OracleDatabaseProperties properties) {
        properties.validate();
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(properties.jdbcUrl());
        dataSource.setUsername(properties.username());
        dataSource.setPassword(properties.password());
        dataSource.setMaximumPoolSize(5);
        dataSource.setMinimumIdle(1);
        dataSource.setConnectionTimeout(5000);
        dataSource.setValidationTimeout(3000);
        return dataSource;
    }
}
