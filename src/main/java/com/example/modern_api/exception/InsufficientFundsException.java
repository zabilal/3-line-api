package com.example.modern_api.exception;

public class InsufficientFundsException extends WalletException {
    public InsufficientFundsException(String message) {
        super(message);
    }
}
