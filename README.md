# 3Line-api

A modern, secure, and resilient Digital Wallet & Payment API built with Spring Boot 4.

## 🚀 Overview

3Line-api is a "bank-grade" payment gateway implementation designed for high reliability and security. It manages user accounts, digital wallets, and peer-to-peer transfers with built-in idempotency and event-driven notifications.

### Key Features
- **JWT Authentication**: Secure, stateless authentication using JSON Web Tokens.
- **Wallet System**: Automatic wallet creation upon registration with support for balance tracking and transaction history.
- **Resilient Transfers**: P2P transfers with mandatory idempotency keys to prevent duplicate processing.
- **Event-Driven Architecture**: Transaction listeners that handle post-transfer actions (e.g., webhook simulations) asynchronously.
- **Comprehensive API Documentation**: Integrated Swagger UI for real-time API exploration.
- **Database Resilience**: PostgreSQL for production/dev and H2 for isolated, fast integration testing.

---

## 🛠️ Technology Stack

- **Framework**: Spring Boot 4.0.3
- **Language**: Java 21+
- **Database**: PostgreSQL (Persistence), H2 (Testing)
- **Security**: Spring Security & JJWT (Json Web Token)
- **Documentation**: SpringDoc OpenAPI / Swagger UI
- **Build Tool**: Maven

---

## 🚦 Getting Started

### Prerequisites
- **Java 21 or higher**
- **PostgreSQL** (running locally or via Docker)
- **Maven** (optional, wrapper included)

### Database Setup
1. Create a PostgreSQL database named `modern_api`:
   ```sql
   CREATE DATABASE modern_api;
   ```
2. Verify credentials in `src/main/resources/application.properties`:
   - Default: User `postgres` / Pass `postgres`

### Installation & Run
1. Clone the repository and navigate to the project root.
2. Build the project:
   ```bash
   ./mvnw clean install
   ```
3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

---

## 📖 API Documentation

Once the application is running, you can access the interactive Swagger documentation at:
- **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **OpenAPI Descriptor**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### Core Endpoints
- `POST /api/users/register`: Register a new user and wallet.
- `POST /api/auth/login`: Authenticate and receive a JWT.
- `GET /api/wallets/balance/{accountNumber}`: Check wallet balance.
- `POST /api/transfers`: Perform a secure transfer (Requires JWT).

---

## 🧪 Testing

The project uses H2 in-memory database for testing to ensure speed and isolation. You do **not** need PostgreSQL running to execute tests.

Run all tests:
```bash
./mvnw test
```

Important test files:
- `WalletIntegrationTest`: Full lifecycle test from registration to transfer.
- `TransferServiceTest`: Core business logic and edge case verification.

---

## 🏗️ Project Structure

```text
src/main/java/com/example/modern_api/
├── config/             # Configuration (Security, OpenAPI)
├── controller/         # REST API Controllers
├── domain/            # JPA Entities (User, Wallet, Transaction)
├── dto/               # Data Transfer Objects (Requests/Responses)
├── event/             # Event Listeners (Asynchronous logic)
├── repository/        # Spring Data JPA Repositories
├── security/          # JWT Filters and Security Services
└── service/           # Business Logic
```

---

## 🔒 Security

Authentication is handled via the `Authorization: Bearer <token>` header. 
- Register a user and login to receive your token.
- The default JWT secret is configured in `application.properties`. **Ensure this is changed for production environments.**

---

## 📄 License
Internal use only for 3Line project.
