package com.example.modern_api.service;

import com.example.modern_api.domain.IdempotencyRecord;
import com.example.modern_api.repository.IdempotencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdempotencyService {
    private final IdempotencyRepository repository;

    @Transactional(readOnly = true)
    public Optional<String> getResponse(String key) {
        return repository.findById(key).map(IdempotencyRecord::getResponsePayload);
    }

    @Transactional
    public void saveResponse(String key, String payload) {
        IdempotencyRecord record = IdempotencyRecord.builder()
                .key(key)
                .responsePayload(payload)
                .build();
        repository.save(record);
    }
}
