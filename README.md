<img width="679" height="546" alt="Screenshot 2026-09-13 at 2 36 28 PM" src="https://github.com/user-attachments/assets/bc61709c-7243-4138-b840-24e73b3abe9b" />
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



<img width="1470" height="956" alt="Screenshot 2026-09-13 at 12 17 33 PM" src="https://github.com/user-attachments/assets/9bfd8077-253c-405d-8080-75dd4d92e575" />
<img width="1470" height="956" alt="Screenshot 2026-09-13 at 12 18 05 PM" src="https://github.com/user-attachments/assets/61c4efc2-75ce-4b0e-ad4d-c282ae02f529" />
<img width="1470" height="956" alt="Screenshot 2026-09-13 at 12 18 30 PM" src="https://github.com/user-attachments/assets/0e181181-9702-48f2-a4d3-a67bd8a943fc" />
<img width="1470" height="956" alt="Screenshot 2026-09-13 at 12 19 07 PM" src="https://github.com/user-attachments/assets/2e9e7958-8b6f-4b52-9699-c62653bace94" />
<img width="685" height="471" alt="Screenshot 2026-09-13 at 12 20 25 PM" src="https://github.com/user-attachments/assets/017cd0ae-aed7-4bdd-a300-7852a90b0343" />
<img width="621" height="476" alt="Screenshot 2026-09-13 at 12 21 04 PM" src="https://github.com/user-attachments/assets/432531ac-d351-436d-927a-f42ffc2d69ee" />
<img width="683" height="474" alt="Screenshot 2026-09-13 at 12 21 41 PM" src="https://github.com/user-attachments/assets/f300a540-de09-4dbc-85cf-307e0c2e5e4c" />
<img width="1470" height="956" alt="Screenshot 2026-09-13 at 12 22 23 PM" src="https://github.com/user-attachments/assets/204c72c5-a18a-49b3-883c-69c284995c94" />
<img width="347" height="268" alt="Screenshot 2026-09-13 at 12 23 00 PM" src="https://github.com/user-attachments/assets/3ecf9630-8e3e-4b19-87fd-fa4720273611" />
<img width="564" height="335" alt="Screenshot 2026-09-13 at 12 24 44 PM" src="https://github.com/user-attachments/assets/429fb60c-7374-4040-b596-c364af2fd293" />
<img width="526" height="506" alt="Screenshot 2026-09-13 at 12 25 40 PM" src="https://github.com/user-attachments/assets/a405a585-53f2-4fd0-9650-ecb7ed7d4dd6" />
<img width="607" height="320" alt="Screenshot 2026-09-13 at 12 25 59 PM" src="https://github.com/user-attachments/assets/cb83bf5e-24a5-4a84-8a5b-3b8649186cef" />
<img width="344" height="281" alt="Screenshot 2026-09-13 at 12 26 16 PM" src="https://github.com/user-attachments/assets/f7c8d13a-5c2d-4ba8-8ac8-4bf5c735104c" />
<img width="599" height="276" alt="Screenshot 2026-09-13 at 12 27 02 PM" src="https://github.com/user-attachments/assets/c289cd5b-996a-4b36-9444-1c92f34c8f3b" />
<img width="625" height="506" alt="Screenshot 2026-09-13 at 12 27 21 PM" src="https://github.com/user-attachments/assets/956a83bb-8af1-4797-9c98-dc84ed9fbdfd" />
<img width="353" height="298" alt="Screenshot 2026-09-13 at 12 28 28 PM" src="https://github.com/user-attachments/assets/e8b4ca7a-61a8-4e3b-b045-4fe0b2a60ce0" />
<img width="1470" height="956" alt="Screenshot 2026-09-13 at 12 28 58 PM" src="https://github.com/user-attachments/assets/68751657-4b80-4355-89e1-df503393a355" />
<img width="368" height="561" alt="Screenshot 2026-09-13 at 12 29 32 PM" src="https://github.com/user-attachments/assets/581d5611-c245-4c0c-a729-d16e23224f8a" />
<img width="689" height="569" alt="Screenshot 2026-09-13 at 12 30 35 PM" src="https://github.com/user-attachments/assets/333f0f6e-f4cc-4aeb-8a8c-4e3d498cc093" />
<img width="341" height="155" alt="Screenshot 2026-09-13 at 12 30 53 PM" src="https://github.com/user-attachments/assets/f5aa7198-fff2-4fdb-b509-aaaac5bdbd69" />
<img width="1470" height="956" alt="Screenshot 2026-09-13 at 12 17 24 PM" src="https://github.com/user-attachments/assets/8d627c40-018b-4ae7-9d54-693ebc68b1ed" />



