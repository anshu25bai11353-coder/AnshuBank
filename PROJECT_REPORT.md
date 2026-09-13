# AnshuBank — Project Report Content

## 1. Introduction
AnshuBank is a local command-line banking simulation built for CSE2006 Programming in Java. It combines object-oriented design, persistence, transaction processing, fraud monitoring, concurrency and reporting in one coherent academic system.

## 2. Problem Statement
A useful Java project should show how language concepts combine to solve a meaningful problem. AnshuBank models customer banking operations while addressing transaction atomicity, suspicious activity, data persistence and concurrent access.

## 3. Objectives
- Apply core Java and OOP concepts in a realistic domain.
- Demonstrate JDBC and SQL transaction handling.
- Demonstrate JPA entities and JPQL.
- Build deterministic fraud-monitoring rules.
- Demonstrate safe concurrent balance updates.
- Generate persistent reports and logs.

## 4. Functional Requirements
Customer management, account creation, authentication, deposit, withdrawal, transfer, transaction history, alerts, statements, password change, admin monitoring, account management, reports and statistics.

## 5. Non-functional Requirements
Performance, security, usability, reliability, maintainability, validation/error handling, logging/monitoring and resource efficiency.

## 6. Architecture
CLI → Service → Repository → H2, with fraud detection and transaction processing as service components. JPA/Hibernate provides an entity/query layer for selected operations.

## 7. Design Diagrams
The Mermaid diagrams in `docs/uml/` and `docs/architecture/` provide the use-case, workflow, class, sequence and ER views.

## 8. Design Decisions
JDBC handles money-transfer-critical work because explicit commit/rollback is easy to demonstrate. JPA is used for entity mapping and JPQL demonstrations rather than duplicating every repository. H2 avoids external database setup. Account-level locks protect shared balances while allowing different accounts to process concurrently.

## 9. Implementation Details
The application initializes the schema from a classpath resource, seeds demo data once through MERGE statements, validates CLI input, hashes passwords, stores transactions and alerts, and generates text reports.

## 10. Results / Screenshots
Run `mvn exec:java` and capture terminal screenshots for the customer dashboard, successful transfer, fraud alert, admin statistics and concurrency monitor. Store them in `docs/screenshots/`.

## 11. Testing Approach
JUnit tests cover password hashing and fraud scoring. Manual tests should cover authentication, CRUD, insufficient balance, transfer atomicity, account state changes, report creation and concurrent withdrawals.

## 12. Challenges
Key engineering challenges are preserving balance consistency under concurrency, coordinating JDBC transaction boundaries, loading schema resources reliably, and using JDBC and JPA without mixing responsibilities.

## 13. Learnings
The project demonstrates that Java concepts are strongest when attached to meaningful responsibilities: inheritance models account types, exceptions represent banking failures, locks protect money state, repositories isolate SQL, and services express business rules.

## 14. Future Enhancements
MFA, stronger password KDF, configurable fraud rules, audit trails, encryption at rest, distributed locking and a richer client could be considered in future versions.

## 15. References
- Jakarta Persistence API documentation
- Hibernate ORM documentation
- H2 Database documentation
- Java SE API documentation
- VITyarthi Build Your Own Project submission guidelines
