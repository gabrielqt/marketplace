package com.marketplace.marketplace.dto.response;

import com.marketplace.marketplace.entity.enums.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
        Long id,
        OrderStatus status,
        BigDecimal total,
        String buyerName,
        List<OrderItemResponse> items
) {
}
