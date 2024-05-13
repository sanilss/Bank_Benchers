package com.example.exception;

public class InvalidMobileNumberFormatException extends RuntimeException {
    public InvalidMobileNumberFormatException(String message) {
        super(message);
    }
}

