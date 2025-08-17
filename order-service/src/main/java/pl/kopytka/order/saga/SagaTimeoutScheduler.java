package pl.kopytka.order.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
class SagaTimeoutScheduler {

    private final OrderSagaRepository orderSagaRepository;

    @Scheduled(cron = "${saga.timeout.cron:0 */5 * * * ?}") // Every 5 minutes
    @SchedulerLock(name = "sagaTimeoutCheck", lockAtMostFor = "PT5M")
    public void markTimeoutSagasAsFailed() {
        Instant cutoffTime = Instant.now().minus(Duration.ofMinutes(10));
        Instant currentTime = Instant.now();

        int failedSagas = orderSagaRepository.markStaleProcessingSagasAsFailed(cutoffTime, currentTime);

        if (failedSagas > 0) {
            log.warn("Marked {} stale sagas as FAILED due to timeout (10 minutes)", failedSagas);
        } else {
            log.debug("No stale sagas found during timeout check");
        }
    }
}
