package pl.kopytka.common.inbox;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;

/**
 * Simple test Avro model for testing IdempotentKafkaConsumer.
 * This class mimics an Avro-generated class for test purposes.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
class TestEventAvroModel extends SpecificRecordBase {

    private String messageId;
    private String content;

    @Override
    public org.apache.avro.Schema getSchema() {
        // For testing purposes, return null - not needed for our test scenario
        return null;
    }

    @Override
    public Object get(int field) {
        return switch (field) {
            case 0 -> messageId;
            case 1 -> content;
            default -> throw new org.apache.avro.AvroRuntimeException("Bad index");
        };
    }

    @Override
    public void put(int field, Object value) {
        switch (field) {
            case 0:
                messageId = (String) value;
                break;
            case 1:
                content = (String) value;
                break;
            default:
                throw new org.apache.avro.AvroRuntimeException("Bad index");
        }
    }
}