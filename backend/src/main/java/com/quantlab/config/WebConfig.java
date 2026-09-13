package com.quantlab.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${quantlab.quant-service.base-url:http://localhost:8000}")
    private String quantServiceBaseUrl;

    public String getQuantServiceBaseUrl() {
        return quantServiceBaseUrl;
    }
}
