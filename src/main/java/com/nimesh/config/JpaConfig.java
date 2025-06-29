package com.nimesh.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.nimesh.repository")
@EntityScan(basePackages = "com.nimesh.model")
public class JpaConfig {
    // JPA configuration
}