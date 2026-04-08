# Database Setup and Migration Guide

## Prerequisites

You need a MySQL database instance running. This guide covers setup and troubleshooting.

## Quick Start

### Option 1: Using Docker (Recommended)

```bash
# Start MySQL container
docker run -d \
  --name amartha-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=amartha_loans_dev \
  -p 3306:3306 \
  mysql:latest

# Wait for MySQL to be ready
sleep 10

# Verify connection
mysql -h localhost -u root -proot -e "SHOW DATABASES;"
```

### Option 2: Using Docker Compose

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:latest
    container_name: amartha-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: amartha_loans_dev
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  mysql-data:
```

Start with:
```bash
docker compose up -d
```

### Option 3: Local MySQL Installation

If you have MySQL installed locally:

```bash
# Start MySQL server (macOS with Homebrew)
brew services start mysql

# Or on Linux
sudo service mysql start

# Create database
mysql -u root -e "CREATE DATABASE IF NOT EXISTS amartha_loans_dev;"
```

## Database Configuration

### Development Mode (Auto-Migration)

```bash
# Run in development mode - Flyway migrates and Hibernate creates schema
./gradlew quarkusDev
```

**What happens:**
1. ✅ Flyway runs migrations (`V1__init.sql`)
2. ✅ Hibernate validates and creates missing tables
3. ✅ Application starts successfully
4. ✅ Ready to use

### Production Mode (Validation Only)

```bash
# In production, schema must exist first
./gradlew quarkusBuild
./java -jar build/quarkus-app/quarkus-run.jar
```

**What happens:**
1. ✅ Flyway runs migrations (must complete successfully)
2. ✅ Hibernate validates schema
3. ✅ Application starts

## Schema Migration Process

### How It Works

1. **Flyway Migration** (`V1__init.sql`):
   - Runs BEFORE application starts
   - Creates all tables if they don't exist
   - Idempotent (safe to run multiple times)

2. **Hibernate Validation**:
   - Runs AFTER Flyway
   - Verifies JPA entities match database schema
   - In dev: auto-fixes mismatches (`drop-and-create`)
   - In prod: fails if mismatch (`validate`)

### Migration File

Location: `src/main/resources/db/migration/V1__init.sql`

Tables created:
- `members` - Base users (borrowers & investors)
- `employees` - Staff (validators & officers)
- `borrower_profiles` - Borrower-specific data
- `investor_profiles` - Investor-specific data
- `loans` - Core loan records
- `loan_approvals` - Approval records (1:1 with loans)
- `loan_investments` - Investment records (N:1 with loans)
- `loan_disbursements` - Disbursement records (1:1 with loans)

## Troubleshooting

### Error: "missing table [borrower_profiles]"

**Cause**: Database exists but tables weren't created.

**Solution**:
```bash
# Option 1: Let Flyway recreate everything
rm -rf ~/.quarkus/mysql-data  # Reset Docker data
docker compose down -v
docker compose up -d
./gradlew quarkusDev

# Option 2: Manually run migration
mysql -h localhost -u root -proot amartha_loans_dev < src/main/resources/db/migration/V1__init.sql
```

### Error: "Connection refused"

**Cause**: MySQL not running or not accessible.

**Solution**:
```bash
# Check if MySQL is running
docker ps | grep mysql

# Or check local MySQL
brew services list

# Start if not running
docker compose up -d
# or
brew services start mysql

# Verify connection
mysql -h localhost -u root -proot -e "SHOW DATABASES;"
```

### Error: "Access denied for user 'root'@'localhost'"

**Cause**: Wrong password or user.

**Solution**:
```bash
# Check credentials in application.properties
grep "quarkus.datasource" src/main/resources/application.properties

# Update if needed
# Default: username=root, password=root

# For custom setup, update:
# quarkus.datasource.username=your_user
# quarkus.datasource.password=your_password
```

### Error: "Database does not exist"

**Cause**: `amartha_loans_dev` database not created.

**Solution**:
```bash
# Create database
mysql -u root -proot -e "CREATE DATABASE IF NOT EXISTS amartha_loans_dev;"

# Verify
mysql -u root -proot -e "SHOW DATABASES;" | grep amartha
```

## Verification

### Check Schema

```bash
# View all tables
mysql -u root -proot amartha_loans_dev -e "SHOW TABLES;"

# Expected output:
# borrower_profiles
# employees
# investor_profiles
# loan_approvals
# loan_disbursements
# loan_investments
# loans
# members
```

### View Table Structure

```bash
# Check members table
mysql -u root -proot amartha_loans_dev -e "DESCRIBE members;"

# Check loans table
mysql -u root -proot amartha_loans_dev -e "DESCRIBE loans;"

# View all indexes
mysql -u root -proot amartha_loans_dev -e "SHOW INDEXES FROM loans;"
```

### Sample Data Insert

```bash
# Insert test member
mysql -u root -proot amartha_loans_dev << 'EOF'
INSERT INTO members (name, email, kyc_status, phone_number)
VALUES ('John Borrower', 'john@example.com', 'VERIFIED', '081234567890');

INSERT INTO employees (role, name, hire_date)
VALUES ('validator', 'Jane Validator', '2026-01-01');

SELECT * FROM members;
EOF
```

## Development Workflow

### First Time Setup

```bash
# 1. Start MySQL
docker compose up -d

# 2. Run application in dev mode
./gradlew quarkusDev

# 3. Check logs for successful startup
# Look for: "Started in X.XXXs"

# 4. Test API
curl http://localhost:8080/q/health
```

### Making Schema Changes

If you need to update the schema:

1. **Create new migration** (e.g., `V2__add_new_column.sql`):
   ```sql
   -- src/main/resources/db/migration/V2__add_new_column.sql
   ALTER TABLE loans ADD COLUMN description VARCHAR(500);
   ```

2. **Run application**:
   ```bash
   ./gradlew quarkusDev
   ```

3. **Flyway automatically applies** the new migration

## Performance Tips

### Indexes

All main tables have indexes on:
- Foreign keys (loan_id, borrower_id, etc.)
- Status fields (loan status, kyc_status)
- Frequently searched fields

### Connection Pooling

In production, configure connection pool:

```properties
quarkus.datasource.reactive.max-size=10
quarkus.datasource.reactive.min-size=2
```

### Query Optimization

```bash
# Enable SQL logging to debug slow queries
quarkus.hibernate-orm.log.sql=true
quarkus.log.category."org.hibernate.SQL".level=DEBUG
```

## Resetting Database

**Development Only:**

```bash
# Option 1: Delete Docker volume and restart
docker compose down -v
docker compose up -d
./gradlew quarkusDev

# Option 2: Drop and recreate
mysql -u root -proot -e "DROP DATABASE amartha_loans_dev; CREATE DATABASE amartha_loans_dev;"
./gradlew quarkusDev

# Option 3: Run Flyway clean (removes migrations)
./gradlew flywayClean
./gradlew quarkusDev
```

## Production Deployment

### Before Deploying

1. **Ensure database exists**:
   ```sql
   CREATE DATABASE amartha_loans;
   ```

2. **Update credentials** in environment:
   ```bash
   export DB_HOST=prod-mysql.example.com
   export DB_NAME=amartha_loans
   export DB_USER=app_user
   export DB_PASSWORD=secure_password
   ```

3. **Update application.properties**:
   ```properties
   %prod.quarkus.datasource.reactive.url=mysql://${DB_HOST:localhost}:3306/${DB_NAME}
   %prod.quarkus.datasource.username=${DB_USER}
   %prod.quarkus.datasource.password=${DB_PASSWORD}
   %prod.quarkus.hibernate-orm.database.generation=validate
   ```

4. **Run migrations**:
   ```bash
   java -Dquarkus.profile=prod \
     -jar build/quarkus-app/quarkus-run.jar
   ```

## Monitoring

### Health Check

```bash
curl http://localhost:8080/q/health
```

Response:
```json
{
  "status": "UP",
  "checks": [
    {
      "name": "Database",
      "status": "UP"
    }
  ]
}
```

### View Logs

```bash
# Dev mode
./gradlew quarkusDev | grep -E "ERROR|INFO|WARN"

# Production
tail -f logs/app.log | grep ERROR
```

## References

- [Quarkus Datasources](https://quarkus.io/guides/datasources)
- [Quarkus Flyway](https://quarkus.io/guides/flyway)
- [Quarkus Hibernate Reactive](https://quarkus.io/guides/hibernate-reactive)
- [MySQL Docker Image](https://hub.docker.com/_/mysql)
- [MySQL Shell](https://dev.mysql.com/doc/mysql-shell/8.0/en/)

## Support

### Common Issues Checklist

- [ ] MySQL is running (`docker ps | grep mysql`)
- [ ] Database exists (`mysql -u root -proot -e "SHOW DATABASES;"`)
- [ ] Credentials match in `application.properties`
- [ ] Flyway migration file exists: `V1__init.sql`
- [ ] No old database locks (`docker compose down -v && docker compose up`)
- [ ] Application logs show successful startup

If issues persist, check:
1. MySQL error logs: `docker logs amartha-mysql`
2. Application logs during startup
3. Database permissions: `GRANT ALL PRIVILEGES ON amartha_loans_dev.* TO 'root'@'localhost';`
