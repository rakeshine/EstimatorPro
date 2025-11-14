package com.rfpe.estimator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class PythonServiceConfig {
    
    @Value("${python.service.url:http://localhost:8000}")
    private String pythonServiceUrl;
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Bean
    public String pythonServiceUrl() {
        return pythonServiceUrl;
    }
}
