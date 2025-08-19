package pl.kopytka.common.outbox;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.UUID;

@Entity(name = "outbox_entries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEntry {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String messageType;

    @Column(nullable = false)
    private String messageKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb", nullable = false)
    private String payload;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant processedAt;

    @Version
    private int version;

    public OutboxEntry(String messageType, String messageKey, String payload) {
        this.id = UUID.randomUUID();
        this.status = OutboxStatus.NEW;
        this.createdAt = Instant.now();
        this.messageType = messageType;
        this.messageKey = messageKey;
        this.payload = payload;
    }

    public void publish() {
        this.status = OutboxStatus.PUBLISHED;
        this.processedAt = Instant.now();
    }

    public void fail() {
        this.status = OutboxStatus.FAILED;
        this.processedAt = Instant.now();
    }
}
