package com.webshop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(@NotNull(message = "A termék ID kötelező!") Long productId,

                               @Min(value = 1, message = "Legalább 1 db terméket kell rendelni!") int quantity) {

}
