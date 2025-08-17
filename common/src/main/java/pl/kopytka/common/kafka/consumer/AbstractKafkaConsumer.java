package pl.kopytka.common.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.beans.factory.annotation.Autowired;
import pl.kopytka.common.TransactionHandler;

import java.util.List;

@Slf4j
public abstract class AbstractKafkaConsumer<T extends SpecificRecordBase> implements KafkaConsumer<T> {

    @Autowired
    private TransactionHandler transactionHandler;

    @Override
    public void receive(List<T> messages,
                        List<String> keys,
                        List<Integer> partitions,
                        List<Long> offsets) {

        log.info("Received {} {} messages", messages.size(), getMessageTypeName());

        messages.forEach(message -> {
            transactionHandler.runInTransaction(() -> processMessage(message));
            log.info("Processed message of type: {} with key: {}, partition: {}, offset: {}",
                    getMessageTypeName(),
                    keys.get(messages.indexOf(message)),
                    partitions.get(messages.indexOf(message)),
                    offsets.get(messages.indexOf(message)));
        });
    }

    protected abstract void processMessage(T message);

    protected abstract String getMessageTypeName();
}