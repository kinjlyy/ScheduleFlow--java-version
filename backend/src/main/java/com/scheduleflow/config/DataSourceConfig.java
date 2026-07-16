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
            if (rawUrl.startsWith("postgres://") || rawUrl.startsWith("postgresql://")) {
                try {
                    String cleanUrl = rawUrl.substring(rawUrl.indexOf("://") + 3);
                    String userInfo = null;
                    String serverAndDb = cleanUrl;
                    int atIndex = cleanUrl.lastIndexOf('@');
                    if (atIndex != -1) {
                        userInfo = cleanUrl.substring(0, atIndex);
                        serverAndDb = cleanUrl.substring(atIndex + 1);
                    }
                    
                    String jdbcUrl = "jdbc:postgresql://" + serverAndDb;
                    if (!jdbcUrl.contains("sslmode=")) {
                        if (jdbcUrl.contains("?")) {
                            jdbcUrl += "&sslmode=require";
                        } else {
                            jdbcUrl += "?sslmode=require";
                        }
                    }
                    dataSourceProperties.setUrl(jdbcUrl);
                    
                    if (userInfo != null) {
                        int colonIndex = userInfo.indexOf(':');
                        String username = userInfo;
                        String password = null;
                        if (colonIndex != -1) {
                            username = userInfo.substring(0, colonIndex);
                            password = userInfo.substring(colonIndex + 1);
                        }
                        
                        username = java.net.URLDecoder.decode(username, java.nio.charset.StandardCharsets.UTF_8);
                        dataSourceProperties.setUsername(username);
                        
                        if (password != null) {
                            password = java.net.URLDecoder.decode(password, java.nio.charset.StandardCharsets.UTF_8);
                            dataSourceProperties.setPassword(password);
                        }
                    }
                } catch (Exception e) {
                    // Fallback to setting the rawUrl if parsing fails
                    dataSourceProperties.setUrl(rawUrl);
                }
            } else {
                dataSourceProperties.setUrl(rawUrl);
            }
        }
        return dataSourceProperties.initializeDataSourceBuilder().build();
    }
}
