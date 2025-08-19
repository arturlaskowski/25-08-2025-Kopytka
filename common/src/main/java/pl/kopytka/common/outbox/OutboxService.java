package pl.kopytka.common.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericContainer;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kopytka.common.outbox.exception.OutboxSerializationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxService {

    private final OutboxRepository outboxRepository;

    @Transactional
    public void save(String messageType, String messageKey, GenericContainer payload) {
        String payloadJson = serializeAvroPayload(payload);

        var entry = new OutboxEntry(messageType, messageKey, payloadJson);
        outboxRepository.save(entry);
    }

    private String serializeAvroPayload(GenericContainer avroRecord) {
        Schema schema = avroRecord.getSchema();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Encoder encoder = EncoderFactory.get().jsonEncoder(schema, out);
            // Prefer SpecificDatumWriter if it's a SpecificRecord, else fallback to GenericDatumWriter
            if (avroRecord instanceof SpecificRecord specificRecord) {
                SpecificDatumWriter<SpecificRecord> writer = new SpecificDatumWriter<>(schema);
                writer.write(specificRecord, encoder);
            } else {
                GenericDatumWriter<GenericContainer> writer = new GenericDatumWriter<>(schema);
                writer.write(avroRecord, encoder);
            }
            encoder.flush();
            return out.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new OutboxSerializationException("Failed to serialize Avro event using Avro encoder", e);
        }
    }
}
