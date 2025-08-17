package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    name "should_create_wallet_successfully"

    request {
        method POST()
        url '/api/wallets'
        headers {
            contentType 'application/json'
        }
        body([
                customerId    : $(anyUuid()),
                initialBalance: $(anyDouble())
        ])
    }

    response {
        status CREATED()
        headers {
            location:
            regex(uuid())
        }
    }
}
