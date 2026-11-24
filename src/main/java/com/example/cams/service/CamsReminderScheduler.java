package com.example.cams.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** SC7: runs the reminder job on a fixed delay (default 15 minutes). */
@Component
@ConditionalOnProperty(name = "cams.mail.scheduler-enabled", havingValue = "true", matchIfMissing = true)
public class CamsReminderScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(CamsReminderScheduler.class);

    private final CamsReminderService reminderService;

    public CamsReminderScheduler(CamsReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @Scheduled(initialDelayString = "${cams.mail.reminder-interval-ms:900000}",
            fixedDelayString = "${cams.mail.reminder-interval-ms:900000}")
    public void run() {
        try {
            var result = reminderService.run();
            if (result.sent() > 0 || result.skipped() > 0) {
                LOG.info("Nhắc lịch: đã gửi {}, bỏ qua {}.", result.sent(), result.skipped());
            }
        } catch (RuntimeException e) {
            LOG.warn("Tác vụ nhắc lịch lỗi: {}", e.toString());
        }
    }
}
