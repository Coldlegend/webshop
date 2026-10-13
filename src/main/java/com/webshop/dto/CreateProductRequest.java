package com.webshop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateProductRequest(@NotBlank(message = "A termék neve nem lehet üres!") String name,
                                   @NotNull(message = "Az ár megadása kötelező!") @Positive(message = "Az árnak " +
                                           "nagyobbnak kell lennie 0-nál!") BigDecimal price,

                                   @Min(value = 0, message = "A készlet nem lehet negatív szám!") int stockQuantity) {
}
