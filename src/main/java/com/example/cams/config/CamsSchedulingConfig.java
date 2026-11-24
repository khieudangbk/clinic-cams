package com.example.cams.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Enables @Scheduled; the reminder job itself is switched by cams.mail.scheduler-enabled. */
@Configuration
@EnableScheduling
public class CamsSchedulingConfig {
}
