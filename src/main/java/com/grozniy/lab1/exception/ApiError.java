package com.grozniy.lab1.exception;

/**
 * Standard error response body returned by the API.
 *
 * @param status  HTTP status code
 * @param error   short machine-readable error identifier
 * @param message human-readable description
 */
public record ApiError(int status, String error, String message) {
}