package com.example.cams;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

import jakarta.annotation.PostConstruct;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class CamsApplication {

    // PostgreSQL
    @PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        EnvLoader.loadEnv();
        SpringApplication.run(CamsApplication.class, args);
    }

}
