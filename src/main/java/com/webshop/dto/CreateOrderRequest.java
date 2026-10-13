package com.webshop.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(
        @NotEmpty(message = "A rendelésnek legalább egy tételt tartalmaznia kell!")
        List<@Valid OrderItemRequest> items

) {
}
