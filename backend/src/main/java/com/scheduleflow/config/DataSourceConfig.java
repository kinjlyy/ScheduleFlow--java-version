package com.scheduleflow.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class DataSourceConfig {

    @Autowired
    private Environment environment;

    @Bean
    public DataSource dataSource(DataSourceProperties dataSourceProperties) {
        String rawUrl = environment.getProperty("DATABASE_URL");
        if (rawUrl != null && !rawUrl.isBlank()) {
            // Convert postgres:// to jdbc:postgresql:// if needed
            if (rawUrl.startsWith("postgres://")) {
                rawUrl = "jdbc:postgresql://" + rawUrl.substring(11);
            }
            dataSourceProperties.setUrl(rawUrl);
        }
        return dataSourceProperties.initializeDataSourceBuilder().build();
    }
}
