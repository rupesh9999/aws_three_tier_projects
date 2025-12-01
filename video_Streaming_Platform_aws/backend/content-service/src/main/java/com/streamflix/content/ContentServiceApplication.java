package com.streamflix.content;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = {"com.streamflix.content", "com.streamflix.common"})
@EnableJpaAuditing
@EnableCaching
public class ContentServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ContentServiceApplication.class, args);
    }
}
