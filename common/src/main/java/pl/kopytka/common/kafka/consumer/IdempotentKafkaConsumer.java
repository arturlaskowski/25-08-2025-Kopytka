package pl.kopytka.common.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.kopytka.common.TransactionHandler;
import pl.kopytka.common.inbox.InboxService;

import java.util.List;

/**
 * Simplified abstract Kafka consumer that implements the Inbox pattern for idempotent message processing.
 * This consumer ensures that each message is processed exactly once based on messageId.
 *
 * @param <T> The type of Avro message to consume
 */
@Slf4j
@Component
public abstract class IdempotentKafkaConsumer<T extends SpecificRecordBase> implements KafkaConsumer<T> {

    @Autowired
    private InboxService inboxService;

    @Autowired
    private TransactionHandler transactionHandler;

    @Override
    public void receive(List<T> messages,
                        List<String> keys,
                        List<Integer> partitions,
                        List<Long> offsets) {

        log.info("Received {} {} messages", messages.size(), getMessageTypeName());

        for (int i = 0; i < messages.size(); i++) {
            T message = messages.get(i);
            String messageKey = keys.get(i);
            Integer partition = partitions.get(i);
            Long offset = offsets.get(i);
            String messageId = extractMessageId(message, messageKey, partition, offset);

            transactionHandler.runInNewTransaction(() ->
                    inboxService.processIfNotExists(messageId, () -> {
                        processMessage(message);
                        log.info("Processed message of type: {} with key: {}, partition: {}, offset: {}",
                                getMessageTypeName(),
                                keys.get(messages.indexOf(message)),
                                partitions.get(messages.indexOf(message)),
                                offsets.get(messages.indexOf(message)));
                    })
            );
        }
    }

    @SuppressWarnings("java:S3011")
    private String extractMessageId(T message, String key, Integer partition, Long offset) {
        try {
            var method = message.getClass().getMethod("getMessageId");
            method.setAccessible(true);
            Object messageId = method.invoke(message);
            if (messageId != null && !messageId.toString().isEmpty()) {
                return messageId.toString();
            }
        } catch (Exception e) {
            log.debug("Failed to extract messageId from message class: {}, error: {}", message.getClass().getSimpleName(), e.getMessage());
        }

        String fallbackId = String.format("%s-%d-%d", key, partition, offset);
        log.debug("Using fallback messageId: {} for key: {}, partition: {}, offset: {}", fallbackId, key, partition, offset);
        return fallbackId;
    }

    protected abstract void processMessage(T message);

    protected abstract String getMessageTypeName();
}