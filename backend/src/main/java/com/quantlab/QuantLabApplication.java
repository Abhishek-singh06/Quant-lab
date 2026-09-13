package com.quantlab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class QuantLabApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuantLabApplication.class, args);
    }
}
