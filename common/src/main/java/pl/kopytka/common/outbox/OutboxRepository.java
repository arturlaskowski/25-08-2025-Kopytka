package pl.kopytka.common.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEntry, UUID> {

    List<OutboxEntry> findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus status);

    @Modifying
    @Query("DELETE FROM outbox_entries e WHERE e.status = 'PUBLISHED' AND e.processedAt < :cutoffTime")
    int deleteOldPublishedEntries(@Param("cutoffTime") Instant cutoffTime);

}
