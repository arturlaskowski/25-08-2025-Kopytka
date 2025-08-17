package pl.kopytka.order.application;


import pl.kopytka.common.domain.valueobject.CustomerId;
import pl.kopytka.common.domain.valueobject.Money;
import pl.kopytka.common.domain.valueobject.OrderId;

public interface ProcessPaymentCommandPublisher {

    void publishProcessPaymentCommand(OrderId orderId, CustomerId customerId, Money price);
}
