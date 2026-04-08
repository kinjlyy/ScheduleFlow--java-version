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
            // Add jdbc: prefix if missing
            if (!rawUrl.startsWith("jdbc:")) {
                rawUrl = "jdbc:" + rawUrl;
            }
            // Add port 5432 if it's missing (Render sometimes omits it)
            if (rawUrl.contains("postgresql://") && !rawUrl.matches(".*:[0-9]+/.*")) {
                rawUrl = rawUrl.replace("postgresql://", "postgresql://").replaceAll("(@[^/]+)/", "$1:5432/");
            }
            dataSourceProperties.setUrl(rawUrl);
        }
        return dataSourceProperties.initializeDataSourceBuilder().build();
    }
}
