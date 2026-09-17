package com.grozniy.lab1.exception;

/** Thrown when a request is malformed or violates input constraints. Maps to HTTP 400. */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}