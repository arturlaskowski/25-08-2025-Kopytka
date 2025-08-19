package pl.kopytka.common.outbox;

import org.junit.jupiter.api.Test;
import pl.kopytka.avro.customer.CustomerEventAvroModel;
import pl.kopytka.avro.customer.CustomerEventType;
import pl.kopytka.common.outbox.exception.OutboxDeserializationException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for OutboxPayloadDeserializer.
 * Tests JSON deserialization back to Avro objects.
 */
class OutboxPayloadDeserializerTest {

    @Test
    void shouldDeserializeValidJsonToAvroObject() {
        // Given
        UUID messageId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        String email = "test@example.com";
        Instant createdAt = Instant.now().truncatedTo(ChronoUnit.MILLIS); // Truncate to match Avro precision

        // Create a valid JSON payload (simulating what OutboxService would create)
        String jsonPayload = String.format("""
                {
                    "messageId": "%s",
                    "customerId": "%s",
                    "email": "%s",
                    "type": "CUSTOMER_CREATED",
                    "createdAt": %d
                }
                """, messageId, customerId, email, createdAt.toEpochMilli());

        // When
        CustomerEventAvroModel result = OutboxPayloadDeserializer.avroJsonToSpecific(
                jsonPayload, CustomerEventAvroModel.class);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessageId()).isEqualTo(messageId);
        assertThat(result.getCustomerId()).isEqualTo(customerId);
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getType()).isEqualTo(CustomerEventType.CUSTOMER_CREATED);
        assertThat(result.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void shouldThrowExceptionForInvalidJson() {
        // Given
        String invalidJson = "{ invalid json }";

        // When & Then
        assertThatThrownBy(() ->
                OutboxPayloadDeserializer.avroJsonToSpecific(invalidJson, CustomerEventAvroModel.class))
                .isInstanceOf(OutboxDeserializationException.class)
                .hasMessageContaining("Failed to deserialize Avro JSON to CustomerEventAvroModel");
    }

    @Test
    void shouldThrowExceptionForMissingRequiredFields() {
        // Given
        String incompleteJson = """
                {
                    "messageId": "123e4567-e89b-12d3-a456-426614174000",
                    "type": "CUSTOMER_CREATED"
                }
                """;

        // When & Then
        assertThatThrownBy(() ->
                OutboxPayloadDeserializer.avroJsonToSpecific(incompleteJson, CustomerEventAvroModel.class))
                .isInstanceOf(OutboxDeserializationException.class);
    }

    @Test
    void shouldThrowExceptionForInvalidEnumValue() {
        // Given
        UUID messageId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        String email = "kon@example.com";

        String jsonWithInvalidEnum = String.format("""
                {
                    "messageId": "%s",
                    "customerId": "%s",
                    "email": "%s",
                    "type": "INVALID_EVENT_TYPE",
                    "createdAt": %d
                }
                """, messageId, customerId, email, Instant.now().toEpochMilli());

        // When & Then
        assertThatThrownBy(() ->
                OutboxPayloadDeserializer.avroJsonToSpecific(jsonWithInvalidEnum, CustomerEventAvroModel.class))
                .isInstanceOf(OutboxDeserializationException.class);
    }

    @Test
    void shouldHandleNullInput() {
        // When & Then
        assertThatThrownBy(() ->
                OutboxPayloadDeserializer.avroJsonToSpecific(null, CustomerEventAvroModel.class))
                .isInstanceOf(OutboxDeserializationException.class);
    }

    @Test
    void shouldHandleEmptyString() {
        // When & Then
        assertThatThrownBy(() ->
                OutboxPayloadDeserializer.avroJsonToSpecific("", CustomerEventAvroModel.class))
                .isInstanceOf(OutboxDeserializationException.class);
    }
}
