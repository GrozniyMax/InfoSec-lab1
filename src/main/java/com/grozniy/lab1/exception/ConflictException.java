package com.grozniy.lab1.exception;

/** Thrown when a resource already exists (e.g. a username is taken). Maps to HTTP 409. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}