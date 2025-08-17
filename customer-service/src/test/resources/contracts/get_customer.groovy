package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    name "should return customer details for valid customer ID"

    request {
        method GET()
        url $('/api/customers/00000000-0000-0000-0000-000000000000')
    }

    response {
        status OK()
        headers {
            contentType 'application/json'
        }
        body([
                id       : $(anyUuid()),
                firstName: $(anyNonBlankString()),
                lastName : $(anyNonBlankString()),
                email    : $(anyNonBlankString())
        ])
    }
}
