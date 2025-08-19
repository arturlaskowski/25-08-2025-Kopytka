package pl.kopytka.common.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxCleaner {

    private final OutboxRepository outboxRepository;

    @Scheduled(cron = "${outbox.cleanup.cron:0 0 2 * * ?}") // Daily at 2 AM
    @SchedulerLock(name = "outboxCleanup", lockAtMostFor = "PT10M")
    @Transactional
    public void cleanup() {
        Instant cutoffTime = Instant.now().minus(Duration.ofDays(7));
        int publishedDeleted = outboxRepository.deleteOldPublishedEntries(cutoffTime);
        log.info("Cleaned up {} published outbox entries", publishedDeleted);
    }
}
