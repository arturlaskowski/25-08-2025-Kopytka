package pl.kopytka.common.outbox;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.kopytka.common.TransactionHandler;

@Component
@Slf4j
@ConditionalOnProperty(name = "kopytka.scheduling.enabled", havingValue = "true")
public abstract class OutboxPublisher {

    @Autowired
    protected OutboxRepository outboxRepository;

    @Autowired
    protected TransactionHandler transactionHandler;

    protected abstract void publish(OutboxEntry entry);

    protected <T extends SpecificRecord> T deserializeAvroPayload(OutboxEntry entry, Class<T> avroType) {
        return OutboxPayloadDeserializer.avroJsonToSpecific(entry.getPayload(), avroType);
    }

    @Scheduled(fixedDelayString = "${outbox.publisher.fixed-delay:2000}") // Default to 2 seconds
    @SchedulerLock(name = "outboxPublisher", lockAtMostFor = "PT30S", lockAtLeastFor = "PT1S")
    public void process() {
        var entries = outboxRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.NEW);
        for (var entry : entries) {
            try {
                transactionHandler.runInTransaction(() -> {
                    publish(entry);
                    entry.publish();
                    outboxRepository.save(entry);
                });
            } catch (Exception e) {
                log.error("Outbox publish failed, entry id={}", entry.getId(), e);
                transactionHandler.runInNewTransaction(() -> {
                    entry.fail();
                    outboxRepository.save(entry);
                });
            }
        }
    }
}
