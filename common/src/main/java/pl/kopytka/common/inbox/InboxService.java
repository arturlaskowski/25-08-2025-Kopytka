package pl.kopytka.common.inbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InboxService {

    private final InboxRepository inboxRepository;

    @Transactional
    public void processIfNotExists(String messageId, Runnable processor) {
        try {
            // Check if entry already exists
            if (inboxRepository.existsById(messageId)) {
                log.debug("Message already processed: {}", messageId);
                return;
            }
            
            inboxRepository.save(new InboxEntry(messageId));
            processor.run();
            log.debug("Message processed: {}", messageId);
        } catch (DataIntegrityViolationException e) {
            log.debug("Message already processed (constraint violation): {}", messageId);
        }
    }
}
