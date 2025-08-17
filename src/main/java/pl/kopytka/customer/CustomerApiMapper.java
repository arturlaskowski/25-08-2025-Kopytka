package pl.kopytka.customer;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
interface CustomerApiMapper {

    CustomerResponse toCustomerResponse(CustomerDto customerDto);

    CreateCustomerDto toCreateCustomerDto(CreateCustomerRequest request);
}