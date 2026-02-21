package com.example.modern_api.exception;

public class ResourceNotFoundException extends WalletException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
