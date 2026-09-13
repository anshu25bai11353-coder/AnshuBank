# System Architecture
```mermaid
flowchart TD
 CLI[CLI Layer] --> S[Service Layer]
 S --> R[Repository Layer]
 R --> DB[(H2 Database)]
 S --> F[Fraud Detection]
 S --> TP[Transaction Processor]
 TP --> W[ExecutorService Workers]
 J[JPA Query Service] --> H[Hibernate/JPA]
 H --> DB
 S --> IO[Reports / Logging]
```
The CLI handles interaction, services own business rules, repositories own JDBC access, and the database stores durable state. Fraud detection and concurrent processing are separate service concerns.
