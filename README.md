# Amartha Loan Service

A robust, event-driven microservice for managing peer-to-peer loan lifecycle management. Built with **Quarkus 3.34**, **Hibernate Reactive + Panache**, **MySQL**, and reactive non-blocking I/O.

---

## 📋 System Overview

The Loan Service enables:

- **Borrowers** to request microloans for various purposes
- **Staff** to validate and approve loans with field evidence
- **Investors** to fund approved loans in tranches
- **Field Officers** to disburse funds upon full funding
- **Automated workflows** via event-driven architecture (agreement letter generation, investor notifications)

**Key constraint:** Loans transition through a strict state machine: `PROPOSED` → `APPROVED` → `INVESTED` → `DISBURSED`

---

## 🔄 Loan Lifecycle State Machine

```
┌─────────┐
│ PROPOSED│ (initial state; awaiting approval)
└────┬────┘
     │ approve() [staff validates with photo proof]
     ↓
┌─────────┐
│ APPROVED│ (ready for investor funding)
└────┬────┘
     │ addInvestment() [investors fund tranches]
     │ [once principal reached → auto-transition]
     ↓
┌─────────┐
│ INVESTED│ (fully funded; awaiting disbursement)
└────┬────┘
     │ disburse() [field officer confirms receipt]
     ↓
┌─────────┐
│DISBURSED│ (terminal state)
└─────────┘
```

---

## 🏗️ Architecture Layers

```
┌─────────────────────────────────────────┐
│  REST API (JAX-RS / RESTEasy Reactive)  │ ← HTTP endpoints, validation, security
├─────────────────────────────────────────┤
│  DTOs & Exception Mappers               │ ← Request/response contracts, error handling
├─────────────────────────────────────────┤
│  Domain Service & Business Logic        │ ← Loan lifecycle, validation, events
├─────────────────────────────────────────┤
│  Domain Model & Validators              │ ← Entities, enums, state rules
├─────────────────────────────────────────┤
│  Repository (Panache Reactive)          │ ← Async data access, queries
├─────────────────────────────────────────┤
│  Infrastructure (PDF, Email, Events)    │ ← PDF generation, Mailer, CDI events
├─────────────────────────────────────────┤
│  Persistence (MySQL + Hibernate Reactive)│ ← Async, non-blocking DB
└─────────────────────────────────────────┘
```

---

## 👥 User Flows

### Staff (Loan Approver)

1. Receives loan request in `PROPOSED` state
2. Conducts field validation (photo proof)
3. Calls `PUT /api/v1/loans/{id}/approve` with approval details
4. System generates PDF agreement letter automatically
5. Loan transitions to `APPROVED`, ready for investor funding

### Investor (Lender)

1. Views approved loans via `GET /api/v1/loans?status=APPROVED`
2. Checks remaining funding gap via `remainingAmount` field
3. Calls `POST /api/v1/investments` to fund a tranche
4. Receives confirmation and tracks investment
5. When loan fully funded → receives notification email (CDI event)

### Field Officer (Disbursement)

1. Verifies loan is `INVESTED` (fully funded)
2. Obtains signed agreement from borrower
3. Calls `PUT /api/v1/loans/{id}/disburse` with signed document path
4. Loan transitions to `DISBURSED`, process complete

---

## 🗄️ Data Model

### Core Entities

**Loan** (aggregate root)

```
├─ id: UUID
├─ borrowerId: String
├─ principalAmount: BigDecimal (positive constraint)
├─ rate: BigDecimal (annual interest %, >= 0)
├─ roi: BigDecimal (annual ROI %, >= 0)
├─ status: LoanStatus (PROPOSED|APPROVED|INVESTED|DISBURSED)
├─ agreementLetterUrl: String (PDF path, set on approval)
├─ createdAt: Timestamp
└─ updatedAt: Timestamp
```

**LoanApproval**

```
├─ loanId: UUID (FK → Loan)
├─ fieldValidatorEmployeeId: String
├─ fieldValidatorPhotoProofPath: String
└─ approvalDate: LocalDate
```

**LoanInvestment**

```
├─ id: UUID
├─ loanId: UUID (FK → Loan)
├─ investorId: String
├─ investorEmail: String
├─ amount: BigDecimal (positive constraint)
└─ createdAt: Timestamp
```

**LoanDisbursement**

```
├─ loanId: UUID (FK → Loan)
├─ fieldOfficerEmployeeId: String
├─ signedAgreementLetterPath: String
└─ disbursementDate: LocalDate
```

---

## 🔌 API Endpoints & Contracts

### Loan Management

#### Create Loan

```
POST /api/v1/loans
Content-Type: application/json
Authorization: Bearer <token> (role: staff|admin)

Request:
{
  "borrowerId": "B-001",
  "principalAmount": 5000000,
  "rate": 12.5,
  "roi": 15.0
}

Response (201 Created):
{
  "id": "uuid",
  "borrowerId": "B-001",
  "principalAmount": 5000000,
  "rate": 12.5,
  "roi": 15.0,
  "status": "PROPOSED",
  "totalInvested": 0,
  "remainingAmount": 5000000,
  "agreementLetterUrl": null,
  "createdAt": "2025-04-06T10:30:00Z"
}
```

#### Get Loan

```
GET /api/v1/loans/{id}
Authorization: Bearer <token>

Response (200):
{
  "id": "uuid",
  "status": "APPROVED",
  "totalInvested": 2500000,
  "remainingAmount": 2500000,
  "agreementLetterUrl": "/tmp/amartha-loans/agreements/uuid_loan-id.pdf",
  "approval": {
    "fieldValidatorEmployeeId": "E-123",
    "approvalDate": "2025-04-06"
  }
  ...
}
```

#### List Loans

```
GET /api/v1/loans?status=APPROVED
Authorization: Bearer <token>

Response (200): Array of LoanResponse
```

#### Approve Loan

```
PUT /api/v1/loans/{id}/approve
Content-Type: application/json
Authorization: Bearer <token> (role: staff|admin)

Request:
{
  "photoProofPath": "/path/to/photo_proof.jpg",
  "employeeId": "E-123",
  "approvalDate": "2025-04-06"
}

Response (200):
{
  "id": "uuid",
  "status": "APPROVED",
  "agreementLetterUrl": "/tmp/amartha-loans/agreements/uuid_agreement.pdf",
  ...
}
```

#### Download Agreement Letter

```
GET /api/v1/loans/{id}/agreement-letter
Accept: application/pdf
Authorization: Bearer <token>

Response (200):
Content-Type: application/pdf
Content-Disposition: attachment; filename="uuid_agreement.pdf"
[PDF binary content]
```

#### Disburse Loan

```
PUT /api/v1/loans/{id}/disburse
Content-Type: application/json
Authorization: Bearer <token> (role: field_officer|admin)

Request:
{
  "signedAgreementLetterPath": "/path/to/signed_agreement.pdf",
  "employeeId": "E-456",
  "disbursementDate": "2025-04-07"
}

Response (200):
{
  "id": "uuid",
  "status": "DISBURSED",
  "disbursement": {
    "fieldOfficerEmployeeId": "E-456",
    "disbursementDate": "2025-04-07"
  }
  ...
}
```

### Investment Management

#### Add Investment

```
POST /api/v1/investments
Content-Type: application/json
Authorization: Bearer <token> (role: investor|admin)

Request:
{
  "loanId": "uuid",
  "investorId": "INV-001",
  "investorEmail": "investor@example.com",
  "amount": 1000000
}

Response (201 Created):
{
  "id": "uuid",
  "loanId": "uuid",
  "investorId": "INV-001",
  "amount": 1000000,
  "createdAt": "2025-04-06T11:00:00Z"
}

Note: When investment reaches principal, loan auto-transitions to INVESTED
      and LoanFullyFundedEvent fires (triggers email notification)
```

#### List Investments for Loan

```
GET /api/v1/investments/{loanId}
Authorization: Bearer <token>

Response (200): Array of investments
```

---

## 🛡️ Security

**Authentication:** HTTP Basic Auth (dev/test) via Quarkus Security Properties File
**Authorization:** Role-based access control (`@RolesAllowed`)

Roles:

- `staff` / `admin` → Create loans, approve
- `field_officer` / `admin` → Disburse
- `investor` / `admin` → Add investments
- `admin` → All operations

**Dev Credentials** (application.properties):

```
staff:staff123
investor:investor123
field_officer:officer123
admin:admin123
```

---

## 🚀 Running the Application

### Prerequisites

- Java 25+
- MySQL 8.0+
- Docker (optional, for local MySQL via Testcontainers in tests)

### Development Mode

```bash
# Start in dev mode (auto-reload, H2 in-memory DB for testing)
./gradlew quarkusDev

# OpenAPI/Swagger UI available at:
# http://localhost:8080/q/swagger-ui

# Health check:
# http://localhost:8080/q/health
```

### Build & Run

```bash
# Build production JAR
./gradlew build -DskipTests

# Run JAR
java -jar build/quarkus-app/quarkus-run.jar
```

### Database Setup

```bash
# Flyway auto-migrates schema on startup
# Migration file: src/main/resources/db/migration/V1__init.sql

# Manual MySQL setup:
CREATE DATABASE amartha_loans;
USE amartha_loans;
-- Flyway executes V1__init.sql automatically
```

---

## 🧪 Testing

### Unit & Integration Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests LoanServiceTest
```

**Test Stack:**

- `@QuarkusTest` for integration testing
- `REST-Assured` for API testing
- `Mockito` for mocking dependencies

### Key Test Cases

- Loan creation with validation
- State transitions (PROPOSED → APPROVED → INVESTED → DISBURSED)
- Investment amount validation (cannot exceed principal)
- Auto-transition to INVESTED when principal reached
- Event firing on full funding
- Exception handling (LoanNotFoundException → 404)
- PDF generation and file storage
- Authorization (role-based endpoint access)

---

## 🛠️ Technology Stack


| Component         | Technology                   | Version   |
| ----------------- | ---------------------------- | --------- |
| **Framework**     | Quarkus                      | 3.34.2    |
| **Java**          | OpenJDK                      | 25+       |
| **Build Tool**    | Gradle                       | 8.x       |
| **ORM**           | Hibernate Reactive + Panache | 2.x       |
| **Database**      | MySQL + H2 (test)            | 8.0 / 2.x |
| **Migration**     | Flyway                       | 9.x       |
| **REST**          | RESTEasy Reactive            | 3.x       |
| **PDF**           | Apache PDFBox                | 3.x       |
| **Email**         | Quarkus Mailer + Mailpit     | Latest    |
| **Validation**    | Jakarta Validation           | 3.x       |
| **Security**      | Quarkus Elytron              | 3.x       |
| **Observability** | SmallRye Health              | 3.x       |


---

## 🔍 Example Workflow

```bash
# 1. Staff creates loan
curl -X POST http://localhost:8080/api/v1/loans \
  -H "Content-Type: application/json" \
  -u "staff:staff123" \
  -d '{"borrowerId":"B-001","principalAmount":1000000,"rate":10,"roi":12}'

# 2. Staff approves with photo proof
curl -X PUT http://localhost:8080/api/v1/loans/{id}/approve \
  -H "Content-Type: application/json" \
  -u "staff:staff123" \
  -d '{"photoProofPath":"/path/photo.jpg","employeeId":"E-1","approvalDate":"2025-04-06"}'

# 3. Investor funds half
curl -X POST http://localhost:8080/api/v1/investments \
  -H "Content-Type: application/json" \
  -u "investor:investor123" \
  -d '{"loanId":"{id}","investorId":"INV-1","investorEmail":"inv1@test.com","amount":500000}'

# 4. Another investor funds remaining half
curl -X POST http://localhost:8080/api/v1/investments \
  -H "Content-Type: application/json" \
  -u "investor:investor123" \
  -d '{"loanId":"{id}","investorId":"INV-2","investorEmail":"inv2@test.com","amount":500000}'
# → Loan auto-transitions to INVESTED; event fires; investors notified

# 5. Officer disburses
curl -X PUT http://localhost:8080/api/v1/loans/{id}/disburse \
  -H "Content-Type: application/json" \
  -u "field_officer:officer123" \
  -d '{"signedAgreementLetterPath":"/path/signed.pdf","employeeId":"O-1","disbursementDate":"2025-04-07"}'
# → Loan transitions to DISBURSED (complete)
```

---

## 📝 Assumptions & Notes

1. **File Storage:** Agreement letters and documents stored locally at `/tmp/amartha-loans/`. For production, using with cloud storage (S3, GCS).
2. **Email Service:** Uses Quarkus Mailer with Mailpit for dev. Configure SMTP in production (`application-prod.properties`).
3. **Borrower/Investor Identity:** Represented as string IDs.
4. **Photo Proof & Signed Agreement:** Opaque string paths provided by client. In production, implement file upload/validation.
5. **Audit Trail:** Basic timestamps (`createdAt`, `updatedAt`). For full audit, add audit tables/events.

---

## 📖 References

- [Quarkus Guide](https://quarkus.io/guides/)
- [Panache Reactive Docs](https://quarkus.io/guides/hibernate-reactive-panache)
- [RESTEasy Reactive](https://quarkus.io/guides/resteasy-reactive)
- [Quarkus Mailer](https://quarkus.io/guides/mailer)
- [Flyway Migrations](https://flywaydb.org/)

