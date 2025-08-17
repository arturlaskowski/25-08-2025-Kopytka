package pl.kopytka.customer.web;

import org.mapstruct.Mapper;
import pl.kopytka.customer.application.dto.CustomerDto;
import pl.kopytka.customer.application.dto.CreateCustomerDto;
import pl.kopytka.customer.web.dto.CustomerResponse;
import pl.kopytka.customer.web.dto.CreateCustomerRequest;

@Mapper(componentModel = "spring")
public interface CustomerApiMapper {

    CustomerResponse toCustomerResponse(CustomerDto customerDto);

    CreateCustomerDto toCreateCustomerDto(CreateCustomerRequest request);
}