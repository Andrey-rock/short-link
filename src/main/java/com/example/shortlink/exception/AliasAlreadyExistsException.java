package com.example.shortlink.exception;

public class AliasAlreadyExistsException extends RuntimeException {
    public AliasAlreadyExistsException() {
        super();
    }

    public AliasAlreadyExistsException(String message) {
        super(message);
    }
}
