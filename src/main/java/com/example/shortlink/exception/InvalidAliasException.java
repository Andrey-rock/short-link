package com.example.shortlink.exception;

public class InvalidAliasException extends RuntimeException {
    public InvalidAliasException() {
        super();
    }

    public InvalidAliasException(String message) {
        super(message);
    }
}
