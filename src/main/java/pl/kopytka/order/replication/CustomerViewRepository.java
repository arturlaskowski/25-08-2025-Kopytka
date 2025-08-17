package pl.kopytka.order.replication;


import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

interface CustomerViewRepository extends CrudRepository<CustomerView, UUID> {
}
