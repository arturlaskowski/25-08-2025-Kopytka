package pl.kopytka.common.kafka.consumer;


import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import pl.kopytka.common.kafka.KafkaConfigData;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig<K extends Serializable, V extends SpecificRecordBase> {

    private final KafkaConfigData kafkaConfigData;
    private final KafkaConsumerConfigData kafkaConsumerConfigData;

    public KafkaConsumerConfig(KafkaConfigData kafkaConfigData,
                               KafkaConsumerConfigData kafkaConsumerConfigData) {
        this.kafkaConfigData = kafkaConfigData;
        this.kafkaConsumerConfigData = kafkaConsumerConfigData;
    }

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfigData.getBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, kafkaConsumerConfigData.getKeyDeserializer());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, kafkaConsumerConfigData.getValueDeserializer());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaConsumerConfigData.getAutoOffsetReset());
        props.put(kafkaConfigData.getSchemaRegistryUrlKey(), kafkaConfigData.getSchemaRegistryUrl());
        props.put(kafkaConsumerConfigData.getSpecificAvroReaderKey(), kafkaConsumerConfigData.getSpecificAvroReader());
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, kafkaConsumerConfigData.getSessionTimeoutMs());
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, kafkaConsumerConfigData.getHeartbeatIntervalMs());
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, kafkaConsumerConfigData.getMaxPollIntervalMs());
        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG,
                kafkaConsumerConfigData.getMaxPartitionFetchBytesDefault() *
                        kafkaConsumerConfigData.getMaxPartitionFetchBytesBoostFactor());
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, kafkaConsumerConfigData.getMaxPollRecords());
        return props;
    }

    @Bean
    public ConsumerFactory<K, V> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<K, V>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<K, V> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setCommonErrorHandler(errorHandler());

        factory.setBatchListener(kafkaConsumerConfigData.getBatchListener());
        factory.setConcurrency(kafkaConsumerConfigData.getConcurrencyLevel());
        factory.setAutoStartup(kafkaConsumerConfigData.getAutoStartup());
        factory.getContainerProperties().setPollTimeout(kafkaConsumerConfigData.getPollTimeoutMs());
        return factory;
    }
/**
     * Wyłączenie retry:
     * <p>
     * Domyślnie Spring Kafka wykonuje 10 prób ponownego przetworzenia wiadomości, jeśli podczas przetwarzania wystąpi wyjątek.
     * Na przykład, jeśli pojawi się wyjątek LoyaltyAccountNotExists, wiadomość zostanie przetworzona aż 10 razy.
     * <p>
     * Zazwyczaj konfigurujemy, które wyjątki powinny być retriowane, a które nie, definiujemy strategię backoff
     * (czyli opóźnienie między próbami) lub przekierowujemy wiadomość na Dead Letter Topic (DLT).
     * To zależy od konfiguracji listenera Kafka w projekcie.
     * <p>
     * Przykład: Można stworzyć abstrakcyjny wyjątek i oznaczyć go jako nieretriowalny:
     * <pre>
     * errorHandler.addNotRetryableExceptions(NonRetryableException.class);
     * </pre>
     * Wszystkie wyjątki, które nie powinny być ponownie przetwarzane, mogą dziedziczyć po NonRetryableException.
     */
   @Bean
    public DefaultErrorHandler errorHandler() {
       return new DefaultErrorHandler(
               (consumerRecord, exception) -> {
                   // Obsługa błędu (np. logowanie)
               },
               new FixedBackOff(0L, 0));
    }
}
