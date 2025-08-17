package pl.kopytka.common.inbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;

public interface InboxRepository extends JpaRepository<InboxEntry, String> {

    @Modifying
    @Query("DELETE FROM inbox_entries e WHERE e.processedAt < :cutoffTime")
    int deleteOldEntries(Instant cutoffTime);
}
