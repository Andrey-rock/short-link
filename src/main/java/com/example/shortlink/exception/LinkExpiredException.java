package com.example.shortlink.exception;

public class LinkExpiredException extends RuntimeException {
    public LinkExpiredException() {
        super("Ссылка истекла");
    }

    public LinkExpiredException(String message) {
        super(message);
    }
}
