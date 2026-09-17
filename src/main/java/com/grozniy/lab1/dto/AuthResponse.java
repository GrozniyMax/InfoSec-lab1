package com.grozniy.lab1.dto;

public record AuthResponse(String token, String tokenType, long expiresIn) {
}