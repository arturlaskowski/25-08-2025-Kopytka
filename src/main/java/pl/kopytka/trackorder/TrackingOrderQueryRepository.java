package pl.kopytka.trackorder;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface TrackingOrderQueryRepository extends JpaRepository<TrackingOrderProjection, UUID> {
}
