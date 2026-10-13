package com.webshop.exceptions;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long id) {
        super("A megadott termék nem található: " + id);
    }
}
