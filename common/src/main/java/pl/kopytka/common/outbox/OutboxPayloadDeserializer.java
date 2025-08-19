package pl.kopytka.common.outbox;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.avro.Schema;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecord;
import pl.kopytka.common.outbox.exception.OutboxDeserializationException;

/**
 * Utility to deserialize Outbox payloads stored as Avro JSON back into SpecificRecord instances.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OutboxPayloadDeserializer {

    /**
     * Deserialize Avro JSON into a SpecificRecord instance of the provided type.
     * The JSON must have been produced by Avro's JSON encoder with the same schema.
     */
    public static <T extends SpecificRecord> T avroJsonToSpecific(String json, Class<T> type) {
        try {
            // Most SpecificRecord implementations expose a static getClassSchema method
            Schema schema = (Schema) type.getMethod("getClassSchema").invoke(null);
            SpecificDatumReader<T> reader = new SpecificDatumReader<>(schema);
            Decoder decoder = DecoderFactory.get().jsonDecoder(schema, json);
            return reader.read(null, decoder);
        } catch (Exception e) {
            throw new OutboxDeserializationException("Failed to deserialize Avro JSON to " + type.getSimpleName(), e);
        }
    }
}
