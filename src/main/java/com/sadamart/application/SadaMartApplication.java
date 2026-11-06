package com.sadamart.application;

import com.sadamart.security.SecurityContextSession;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = "com.sadamart")
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class
})
public class SadaMartApplication {

    @Bean
    public SecurityContextSession securityContextSession() {
        return new SecurityContextSession();
    }

    public static ConfigurableApplicationContext startNonWebContext(String... args) {
        SpringApplication app = new SpringApplication(SadaMartApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        return app.run(args);
    }
}
