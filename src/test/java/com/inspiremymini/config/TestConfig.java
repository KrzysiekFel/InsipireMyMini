package com.inspiremymini.config;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class TestConfig {
    @Bean
    public TestRestTemplate restTemplate() {
        return new TestRestTemplate();
    }
}
