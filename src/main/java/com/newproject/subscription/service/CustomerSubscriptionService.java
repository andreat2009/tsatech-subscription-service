package com.newproject.subscription.service;

import com.newproject.subscription.domain.CustomerSubscription;
import com.newproject.subscription.dto.CustomerSubscriptionRequest;
import com.newproject.subscription.dto.CustomerSubscriptionResponse;
import com.newproject.subscription.events.EventPublisher;
import com.newproject.subscription.exception.NotFoundException;
import com.newproject.subscription.repository.CustomerSubscriptionRepository;
import com.newproject.subscription.security.RequestActor;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerSubscriptionService {
    private final CustomerSubscriptionRepository repository;
    private final EventPublisher eventPublisher;
    private final RequestActor requestActor;

    public CustomerSubscriptionService(CustomerSubscriptionRepository repository, EventPublisher eventPublisher, RequestActor requestActor) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.requestActor = requestActor;
    }

    @Transactional(readOnly = true)
    public List<CustomerSubscriptionResponse> list(Long customerId) {
        requestActor.assertCustomerAccessIfAuthenticated(customerId);
        return repository.findByCustomerIdOrderByCreatedAtDesc(customerId)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public CustomerSubscriptionResponse create(Long customerId, CustomerSubscriptionRequest request) {
        requestActor.assertCustomerAccessIfAuthenticated(customerId);
        CustomerSubscription subscription = new CustomerSubscription();
        subscription.setCustomerId(customerId);
        apply(subscription, request, true);
        OffsetDateTime now = OffsetDateTime.now();
        subscription.setCreatedAt(now);
        subscription.setUpdatedAt(now);

        CustomerSubscription saved = repository.save(subscription);
        CustomerSubscriptionResponse response = toResponse(saved);
        eventPublisher.publish("SUBSCRIPTION_CREATED", "customer_subscription", saved.getId().toString(), response);
        return response;
    }

    @Transactional
    public CustomerSubscriptionResponse update(Long customerId, Long subscriptionId, CustomerSubscriptionRequest request) {
        requestActor.assertCustomerAccessIfAuthenticated(customerId);
        CustomerSubscription subscription = repository.findByIdAndCustomerId(subscriptionId, customerId)
            .orElseThrow(() -> new NotFoundException("Subscription not found"));

        apply(subscription, request, false);
        subscription.setUpdatedAt(OffsetDateTime.now());

        CustomerSubscription saved = repository.save(subscription);
        CustomerSubscriptionResponse response = toResponse(saved);
        eventPublisher.publish("SUBSCRIPTION_UPDATED", "customer_subscription", saved.getId().toString(), response);
        return response;
    }

    private void apply(CustomerSubscription subscription, CustomerSubscriptionRequest request, boolean createMode) {
        if (createMode || request.getPlanName() != null) {
            subscription.setPlanName(request.getPlanName());
        }
        if (createMode || request.getStatus() != null) {
            subscription.setStatus(defaultStatus(request.getStatus()));
        }
        if (createMode || request.getAmount() != null) {
            subscription.setAmount(request.getAmount() != null ? request.getAmount() : BigDecimal.ZERO);
        }
        if (createMode || request.getCurrency() != null) {
            subscription.setCurrency(defaultCurrency(request.getCurrency()));
        }
        if (createMode || request.getNextBillingAt() != null) {
            subscription.setNextBillingAt(request.getNextBillingAt());
        }
    }

    private String defaultStatus(String status) {
        if (status == null || status.isBlank()) {
            return "ACTIVE";
        }
        return status.trim().toUpperCase(Locale.ROOT);
    }

    private String defaultCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            return "EUR";
        }
        return currency.trim().toUpperCase(Locale.ROOT);
    }

    private CustomerSubscriptionResponse toResponse(CustomerSubscription subscription) {
        CustomerSubscriptionResponse response = new CustomerSubscriptionResponse();
        response.setId(subscription.getId());
        response.setCustomerId(subscription.getCustomerId());
        response.setPlanName(subscription.getPlanName());
        response.setStatus(subscription.getStatus());
        response.setAmount(subscription.getAmount());
        response.setCurrency(subscription.getCurrency());
        response.setNextBillingAt(subscription.getNextBillingAt());
        response.setCreatedAt(subscription.getCreatedAt());
        response.setUpdatedAt(subscription.getUpdatedAt());
        return response;
    }
}
