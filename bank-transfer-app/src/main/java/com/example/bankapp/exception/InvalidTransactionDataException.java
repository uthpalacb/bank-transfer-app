package com.example.bankapp.exception;

public class InvalidTransactionDataException extends RuntimeException {
    public InvalidTransactionDataException(String message) {
        super(message);
    }
}