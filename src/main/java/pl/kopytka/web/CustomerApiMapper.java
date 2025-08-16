package pl.kopytka.web;

import org.mapstruct.Mapper;
import pl.kopytka.application.dto.CreateCustomerDto;
import pl.kopytka.application.dto.CustomerDto;
import pl.kopytka.web.dto.CreateCustomerRequest;

@Mapper(componentModel = "spring")
public interface CustomerApiMapper {

    CustomerResponse toCustomerResponse(CustomerDto customerDto);

    CreateCustomerDto toCreateCustomerDto(CreateCustomerRequest request);
}