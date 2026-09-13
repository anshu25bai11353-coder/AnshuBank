# AnshuBank — Banking Transaction and Fraud Monitoring System

A professional **Java 21 command-line academic project** for CSE2006 Programming in Java. It simulates customer banking operations and administrative fraud monitoring using H2, JDBC, JPA/Hibernate, collections, exceptions, file I/O and multithreading.

> **Disclaimer:** This is an educational banking simulation and is not intended for real financial transactions or production banking security.

## Problem Statement
Banking examples frequently stop at basic CRUD. AnshuBank demonstrates a broader real-world workflow: authenticated users perform money operations, transactions are persisted atomically, suspicious activity is scored by deterministic rules, alerts are stored for admin review, reports are generated, and concurrent operations are protected with account-level locks.

## Features
- Customer login and account creation
- Savings and current accounts
- Deposit, withdrawal and transfer
- JDBC commit/rollback for transfers
- Rule-based fraud monitoring (not ML)
- Fraud alerts and admin review
- Account activation/suspension/closure
- Transaction history and statements
- Concurrent processing with ExecutorService
- H2 embedded database
- JDBC repositories and JPA/JPQL query layer
- Reports and structured logging
- JUnit tests

## Architecture
```text
CLI -> Services -> Repositories -> H2
             |          
             +-> Fraud Detection
             +-> Transaction Processor -> ExecutorService
JPA Query Service -> Hibernate/JPA -> H2
```

## Project Structure
`src/main/java/com/anshubank/` contains model, entity, repository, service, database, concurrency, security, utility, exception and CLI layers. Runtime resources are in `src/main/resources/`. Reports, logs and the embedded database use `reports/`, `logs/` and `data/`.

## Requirements
- JDK 21+
- Maven 3.9+

The Maven compiler target is explicitly Java 21 even if a newer JDK is installed.

## How to Run

### Requirements
- Java 21
- Maven 3.8+
- Git

### Clone the Repository

git clone https://github.com/anshu25bai11353-coder/AnshuBank.git

cd AnshuBank

### Compile

mvn clean compile

### Run

mvn exec:java

The application will start as a command-line banking system.

### Alternative

The project can also be opened in VS Code or IntelliJ IDEA as a Maven project and run from the Java application entry point.`

Primary entry point: `com.anshubank.Main`.

## Demo Credentials
**Admin:** `admin` / `AnshuBank@123`  
**Customer:** `C1001` / `Customer@123`  
**Demo account:** `SB10001` with an initial balance of Rs. 50000.

Passwords are stored as SHA-256 hashes in this educational project and are never logged. Production banking should use a dedicated password-hashing/KDF strategy, secure secret management and substantially stronger controls.

## Database
H2 runs in an embedded local file under `data/`. The runtime schema is loaded from the classpath resource `src/main/resources/schema.sql`, avoiding a working-directory-dependent `sql/schema.sql` lookup. The same schema is copied to `sql/schema.sql` for repository visibility.

JDBC is used for transaction-critical operations and repositories. JPA/Hibernate is included for entity mapping and JPQL demonstrations; this avoids duplicating all business logic in two persistence technologies.

## Fraud Rules
- Amount >= Rs. 50,000 adds large-transaction risk.
- Amount >= Rs. 100,000 adds higher risk.
- Multiple recent transactions add frequency risk.
- Repeated high-frequency activity adds pattern risk.
- Score is capped at 100 and mapped to LOW/MEDIUM/HIGH/CRITICAL.

Suspicious transactions are flagged rather than automatically blocked in every case.

## Concurrency
`TransactionProcessor` uses a fixed `ExecutorService` with three workers. `TransactionService` uses per-account locks and lock ordering for transfers so different accounts can process concurrently while the same account is protected from race conditions. The admin menu includes a controlled withdrawal test.

## Testing
Run `mvn test`. Important scenarios to verify manually include:
1. Deposit/withdrawal validation.
2. Insufficient balance.
3. Transfer success.
4. Transfer rollback by forcing a database-side failure in a test scenario.
5. Large transaction fraud alert.
6. Concurrent Rs. 30,000 and Rs. 25,000 withdrawals from a Rs. 50,000 account: only one can succeed.

## Documentation
See `docs/architecture/`, `docs/uml/`, and `statement.md` for maintained Mermaid diagrams and project scope. Screenshots can be added under `docs/screenshots/` after running the application.

## Non-Functional Requirements
- **Performance:** bounded worker pool and database-side filtering.
- **Security:** hashed passwords, authentication, prepared statements, validation and no password logging.
- **Usability:** readable CLI menus and graceful input errors.
- **Reliability:** JDBC transactions, rollback and account-level synchronization.
- **Maintainability:** layered packages, repositories and services.
- **Error handling:** specific checked exceptions and menu-level recovery.
- **Logging/monitoring:** structured file logging and processing monitor.
- **Resource efficiency:** try-with-resources and ExecutorService shutdown.

## Course Concept Mapping
| CSE2006 concept | AnshuBank implementation |
|---|---|
| Classes/Objects | Customer, Account, Transaction, services |
| Constructors | Domain/model constructors |
| Encapsulation | Private fields and controlled methods |
| Inheritance | SavingsAccount/CurrentAccount extend Account |
| Overloading | `deposit(amount)` and `deposit(amount, description)` |
| Overriding | Account type implementation |
| Runtime polymorphism | Account references pointing to concrete account types |
| Abstract class | Account |
| Interface | `DatabaseManager.SQLConsumer` functional interface |
| enum | AccountType, RiskLevel, statuses and transaction types |
| static/final | Singleton manager, constants and immutable fields |
| Nested class | `FraudDetectionService.RuleResult` record |
| Singleton | Thread-safe DatabaseManager |
| Exceptions | Banking-specific checked exceptions |
| Collections | List, Map, CopyOnWriteArrayList, ConcurrentHashMap |
| File I/O | Statements/reports/logging |
| Multithreading | ExecutorService workers |
| Synchronization | Account-specific locks |
| JDBC | Repositories and atomic transfer |
| SQL | H2 schema and prepared statements |
| JPA | Entity mappings and JPQL queries |
| CRUD | Customer/account/admin/transaction repositories |

## GitHub
```bash
git init
git add .
git commit -m "Initial AnshuBank project"
git branch -M main
git remote add origin <REPOSITORY_URL>
git push -u origin main
```

Suggested commits: setup, domain models, authentication, accounts, transactions/JDBC, fraud detection, multithreading, reports/I/O, JPA, tests, documentation, final cleanup.

## Future Enhancements
This simulator does not implement real payment rails, external KYC, distributed locking, encryption-at-rest, MFA or machine-learning fraud detection. Future versions could add stronger password KDFs, audit trails, configurable rule engines and a desktop/web client, while keeping the academic CLI version intact.
