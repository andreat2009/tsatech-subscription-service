package com.newproject.subscription.repository;

import com.newproject.subscription.domain.CustomerSubscription;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerSubscriptionRepository extends JpaRepository<CustomerSubscription, Long> {
    List<CustomerSubscription> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    Optional<CustomerSubscription> findByIdAndCustomerId(Long id, Long customerId);
}
