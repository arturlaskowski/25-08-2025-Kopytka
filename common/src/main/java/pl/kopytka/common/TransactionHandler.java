package pl.kopytka.common;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransactionHandler {

    @Transactional(propagation = Propagation.REQUIRED)
    public void runInTransaction(Runnable codeBlock) {
        codeBlock.run();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void runInNewTransaction(Runnable codeBlock) {
        codeBlock.run();
    }

}