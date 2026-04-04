package com.marketplace.marketplace.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(

        @NotEmpty(message = "Order must have at leat one item")
        List<@Valid OrderItemRequest> items
) {
}
