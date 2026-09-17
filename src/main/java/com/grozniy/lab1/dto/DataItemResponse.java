package com.grozniy.lab1.dto;

import java.time.Instant;

public record DataItemResponse(Long id, String title, String content, Instant createdAt) {
}