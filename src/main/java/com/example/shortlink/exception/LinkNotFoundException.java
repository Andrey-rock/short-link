package com.example.shortlink.exception;

public class LinkNotFoundException extends RuntimeException {
    public LinkNotFoundException() {
        super();
    }

    public LinkNotFoundException(String message) {
        super(message);
    }
}
