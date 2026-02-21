# Wallet System Implementation Plan

This document outlines the architecture and implementation steps for a production-ready Wallet System with 100% test coverage.

## 1. Technical Stack
- **Framework**: Spring Boot 4.0.3
- **Language**: Java 21
- **Database**: H2 (In-memory for demo, JPA for portability)
- **Security**: Optimistic Locking (`@Version`), Idempotency Keys
- **Testing**: JUnit 5, Mockito, AssertJ, MockMvc

## 2. Core Entities
- **User**: System user identity.
- **Wallet**: Financial account containing balance, status, and versioning.
- **Transaction**: Immutable ledger recording all fund movements (including fees).
- **IdempotencyRecord**: Tracks request keys to prevent duplicate processing.

## 3. Features to Implement
- [ ] **Account Generation**: Auto-creation of wallets upon user registration with unique account numbers.
- [ ] **Fund Transfer**: Atomic transfers with double-entry style records.
- [ ] **Fees Engine**: Automatic deduction of system fees during transfers.
- [ ] **Concurrency Control**: Prevents race conditions on balance updates using Optimistic Locking.
- [ ] **Idempotency**: Middleware/Service to handle duplicate request keys.
- [ ] **Event System**: Spring `@EventListener` for post-transaction activities (e.g., logging, simulated webhooks).
- [ ] **Validation**: Comprehensive check for negative amounts, self-transfers, and account status.

## 4. Implementation Steps
### Phase 1: Domain & Persistence
1. Define Entities (`User`, `Wallet`, `Transaction`, `IdempotencyRecord`).
2. Implement Repositories with necessary query methods.

### Phase 2: Core Services
1. `UserService`: Registration logic.
2. `WalletService`: Lookup and balance management.
3. `TransferService`: The heavy lifting of fund transfers (Transactional).
4. `IdempotencyService`: Logic to check/store request keys.

### Phase 3: API Layer
1. `UserController`: Endpoints for user/wallet creation.
2. `TransferController`: Endpoint for initiating transfers.
3. Global Exception Handler.

### Phase 4: Testing & Coverage
1. Unit tests for all services.
2. Integration tests for API endpoints.
3. Verification of 100% branch/line coverage.

---
**Status**: Initializing Project Structure...
