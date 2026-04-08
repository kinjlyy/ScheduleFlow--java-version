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
        String rawUrl = environment.getProperty("SPRING_DATASOURCE_URL");
        if (rawUrl != null && !rawUrl.isBlank()) {
            if (!rawUrl.startsWith("jdbc:")) {
                rawUrl = "jdbc:" + rawUrl;
            }
            dataSourceProperties.setUrl(rawUrl);
        }
        return dataSourceProperties.initializeDataSourceBuilder().build();
    }
}
