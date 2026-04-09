package com.example.shortlink.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record ResponseError(String message, Instant timestamp) {
}
