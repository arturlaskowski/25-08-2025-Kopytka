package pl.kopytka.trackorder;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.kopytka.common.event.OrderChangedEvent;

@Component
@RequiredArgsConstructor
class OrderEventHandler {

    private final TrackingOrderQueryRepository trackingOrderQueryRepository;

    @EventListener
    public void handle(OrderChangedEvent event) {
        var trackingOrderProjection =
                TrackingOrderProjection.builder()
                        .orderId(event.orderId())
                        .status(event.orderStatus())
                        .amount(event.amount())
                        .build();
        trackingOrderQueryRepository.save(trackingOrderProjection);
    }
}