package pl.kopytka.common.inbox;

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
public class InboxCleaner {

    private final InboxRepository inboxRepository;

    @Scheduled(cron = "${inbox.cleanup.cron:0 0 3 * * ?}") // Daily at 3 AM
    @SchedulerLock(name = "inboxCleanup", lockAtMostFor = "PT10M")
    @Transactional
    public void cleanup() {
        Instant cutoffTime = Instant.now().minus(Duration.ofDays(7));
        int deletedEntries = inboxRepository.deleteOldEntries(cutoffTime);
        log.info("Cleaned up {} processed inbox entries", deletedEntries);
    }
}
