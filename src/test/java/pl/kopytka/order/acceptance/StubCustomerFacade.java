package pl.kopytka.order.acceptance;


import lombok.Setter;
import pl.kopytka.customer.CustomerFacade;

import java.util.UUID;

@Setter
class StubCustomerFacade implements CustomerFacade {

    private boolean customerExists = true;

    @Override
    public boolean existsById(UUID customerId) {
        return customerExists;
    }
}