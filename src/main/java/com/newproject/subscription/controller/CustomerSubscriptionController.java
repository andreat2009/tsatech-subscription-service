package com.newproject.subscription.controller;

import com.newproject.subscription.dto.CustomerSubscriptionRequest;
import com.newproject.subscription.dto.CustomerSubscriptionResponse;
import com.newproject.subscription.service.CustomerSubscriptionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers/{customerId}/subscriptions")
public class CustomerSubscriptionController {
    private final CustomerSubscriptionService service;

    public CustomerSubscriptionController(CustomerSubscriptionService service) {
        this.service = service;
    }

    @GetMapping
    public List<CustomerSubscriptionResponse> list(@PathVariable Long customerId) {
        return service.list(customerId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerSubscriptionResponse create(
        @PathVariable Long customerId,
        @Valid @RequestBody CustomerSubscriptionRequest request
    ) {
        return service.create(customerId, request);
    }

    @PutMapping("/{subscriptionId}")
    public CustomerSubscriptionResponse update(
        @PathVariable Long customerId,
        @PathVariable Long subscriptionId,
        @Valid @RequestBody CustomerSubscriptionRequest request
    ) {
        return service.update(customerId, subscriptionId, request);
    }
}
