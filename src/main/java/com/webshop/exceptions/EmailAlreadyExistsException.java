package com.webshop.exceptions;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super("Ez az e-mail cím már regisztrálva van: " + email);
    }
}