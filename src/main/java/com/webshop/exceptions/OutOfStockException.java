package com.webshop.exceptions;

public class OutOfStockException extends RuntimeException {
    public OutOfStockException(String productName, int available, int requested) {
        super(String.format("Nincs elég készlet a(z) '%s' termékből! Elérhető: %d, kért: %d",
                productName, available, requested));
    }
}
