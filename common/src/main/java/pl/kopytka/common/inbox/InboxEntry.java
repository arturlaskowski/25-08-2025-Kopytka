package pl.kopytka.common.inbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity(name = "inbox_entries")
@NoArgsConstructor
@Getter
public class InboxEntry {

    @Id
    private String messageId;

    @Column(nullable = false)
    private Instant processedAt;

    public InboxEntry(String messageId) {
        this.messageId = messageId;
        this.processedAt = Instant.now();
    }
}
