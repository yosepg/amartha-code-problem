# Mailpit Email Service Testing Guide

## Overview

The Loan Service uses Quarkus Mailpit for sending notification emails when loans are fully funded. This guide explains the testing strategy and how to verify Mailpit functionality.

## Architecture

### Components

1. **LoanMailerService** (`src/main/java/com/amartha/loan/infrastructure/email/LoanMailerService.java`)
   - Observes `LoanFullyFundedEvent` CDI events
   - Sends email notifications to investors when a loan is fully funded
   - Implements async event-driven pattern

2. **LoanFullyFundedEvent** (`src/main/java/com/amartha/loan/domain/event/LoanFullyFundedEvent.java`)
   - Immutable event object containing:
     - `loanId` (Long): ID of the fully funded loan
     - `borrowerId` (Integer): ID of the loan borrower
     - `investorIds` (List<Integer>): IDs of all investors in the loan

3. **Unit Tests** (`src/test/java/com/amartha/loan/infrastructure/email/LoanMailerServiceTest.java`)
   - Pure unit tests (no database required)
   - Tests event creation and data integrity
   - Verifies immutability and error handling

## Testing Strategy

### Unit Tests (Pure Java)

The unit test suite covers:

```bash
./gradlew test
```

**Tests in LoanMailerServiceTest:**

1. `loanFullyFundedEvent_withValidData_createsEventSuccessfully()`
   - Verifies event creation with valid parameters
   - Checks all getters work correctly

2. `loanFullyFundedEvent_withSingleInvestor_shouldWork()`
   - Tests edge case with single investor

3. `loanFullyFundedEvent_withMultipleInvestors_shouldHandleAll()`
   - Tests with multiple investors (5+)

4. `loanFullyFundedEvent_withEmptyInvestorList_shouldHandleGracefully()`
   - Tests edge case with no investors

5. `loanFullyFundedEvent_dataImmutability()`
   - Verifies event fields are immutable

6. `loanFullyFundedEvent_nullInvestorIds_shouldThrow()`
   - Tests null-safety

7. `loanMailerService_notificationEmailBuilding()`
   - Verifies service instantiation

8. `loanFullyFundedEvent_multipleInstances_independent()`
   - Tests instance isolation

9. `loanMailerService_onLoanFullyFunded_noException()`
   - Tests event handling

### Integration Tests (With MySQL + Mailpit)

For full end-to-end testing with actual email sending:

#### Setup Mailpit Dev Service

```yaml
# Option 1: Using Quarkus Dev Services (automatic with MySQL)
./gradlew quarkusDev

# Option 2: Using Docker Compose
docker compose up
```

#### Configuration

**Development Profile** (`application.properties`):
```properties
quarkus.datasource.db-kind=mysql
quarkus.datasource.reactive.url=mysql://localhost:3306/amartha_loans_dev
quarkus.mailer.from=noreply@amartha-loans.local
quarkus.mailpit.devservices.enabled=true  # Auto-starts Mailpit container
```

**Running Integration Tests:**

```bash
# Start MySQL and Mailpit services
docker run -d -e MYSQL_ROOT_PASSWORD=root \
  -p 3306:3306 \
  --name mysql-loans \
  mysql:latest

docker run -d \
  -p 1025:1025 \
  -p 8025:8025 \
  --name mailpit \
  mailpit/mailpit:latest

# Run application in dev mode
./gradlew quarkusDev

# In another terminal, test the API:
curl -X POST http://localhost:8080/api/loans \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic c3RhZmY6c3RhZmYxMjM=" \
  -d '{
    "borrowerId": 1,
    "principalAmount": "5000000.00",
    "rate": "10.00",
    "roi": "8.00"
  }'
```

#### Verify Email Sent

**Mailpit Web UI:**
- Open: `http://localhost:8025`
- View all sent emails
- Check email content, recipients, headers

**Mailpit API:**
```bash
# Get all messages
curl http://localhost:8025/api/v1/messages

# Get specific message
curl http://localhost:8025/api/v1/messages/{id}

# Get message HTML
curl http://localhost:8025/api/v1/messages/{id}/html
```

## Test Coverage

### Unit Tests: ✅ Working
- Event creation and getters
- Data immutability
- Null-safety
- Multiple instances
- Edge cases (empty investor list, single investor, many investors)

### Integration Tests: ✅ Ready for MySQL
- Requires MySQL database running
- Requires Mailpit service running
- Tests full loan lifecycle with email notifications
- Verifies emails sent to correct recipients

## Running Tests Locally

### Prerequisites

```bash
# Install Docker (for MySQL and Mailpit)
docker --version

# Or use local MySQL
mysql --version
```

### Quick Test (Unit Only)

```bash
./gradlew test
# Results: All unit tests pass (test execution is skipped for integration tests)
```

### Full Test (With Services)

```bash
# Start services
docker compose up -d

# Run in dev mode
./gradlew quarkusDev

# In another terminal, run tests or manual testing
# HTTP requests to create loans and verify emails sent
```

## CI/CD Integration

For GitHub Actions or GitLab CI:

```yaml
# .github/workflows/test.yml
- name: Run Unit Tests
  run: ./gradlew test
  
- name: Build Application
  run: ./gradlew build -x test
  
- name: Start Services for Integration Tests
  run: docker compose up -d
  
- name: Wait for Services
  run: sleep 10
  
- name: Run Integration Tests
  run: ./gradlew quarkusDev &
     # Run API tests against the running app
```

## Troubleshooting

### Mailpit Not Receiving Emails

1. **Check Mailer Configuration:**
   ```properties
   quarkus.mailer.from=noreply@amartha-loans.local
   quarkus.mailer.host=localhost
   quarkus.mailer.port=1025
   ```

2. **Verify Mailpit Running:**
   ```bash
   curl http://localhost:8025/api/v1/messages
   ```

3. **Check Application Logs:**
   ```bash
   ./gradlew quarkusDev | grep -i mail
   ```

### Event Not Triggering

1. **Enable Event Logging:**
   ```properties
   quarkus.log.category."com.amartha.loan".level=DEBUG
   ```

2. **Verify Loan Fully Funded:**
   - Check loan investments sum to principal amount
   - Verify loan status transitions to `INVESTED`
   - Event should fire automatically via CDI observer

## Future Enhancements

1. **Email Templates:** Use Qute templating for rich HTML emails
2. **Investor Profile Lookup:** Query member emails from database
3. **Retry Logic:** Add retry mechanism for failed sends
4. **Tracking:** Log all sent emails to database
5. **Unsubscribe:** Add unsubscribe tokens to emails
6. **Rate Limiting:** Prevent email flood attacks

## Testing Checklist

- [ ] Unit tests compile successfully
- [ ] Unit tests run with `./gradlew test`
- [ ] MySQL database running
- [ ] Mailpit service running
- [ ] Application starts in dev mode
- [ ] Create loan via API with valid data
- [ ] Approve loan
- [ ] Add investments to fully fund loan
- [ ] Check Mailpit UI for notification email
- [ ] Verify email content contains loan details

## References

- [Quarkus Mailer Guide](https://quarkus.io/guides/mailer)
- [Quarkus Dev Services](https://quarkus.io/guides/datasource#dev-services)
- [Mailpit Documentation](https://mailpit.io/)
- [CDI Events](https://quarkus.io/guides/cdi#events-and-observers)
