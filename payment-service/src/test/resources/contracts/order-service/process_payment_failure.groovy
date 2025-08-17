package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    name "should fail payment when insufficient funds"

    request {
        method POST()
        url '/api/payments/process'
        headers {
            contentType 'application/json'
        }
        body([
                orderId   : "00000000-0000-0000-0000-000000000001",
                customerId: $(anyUuid()),
                price     : $(anyDouble())
        ])
    }

    response {
        status OK()
        headers {
            contentType 'application/json'
        }
        body([
                paymentId   : $(anyUuid()),
                success     : false,
                errorMessage: $(anyNonBlankString())
        ])
    }
}
