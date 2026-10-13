package com.webshop.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String userEmail,
        BigDecimal totalPrice,
        LocalDateTime createdAt,
        List<OrderItemResponse> items
) {}
