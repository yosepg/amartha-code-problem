-- Members table (base for borrowers and investors)
CREATE TABLE members (
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT 'Member ID',
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    kyc_status VARCHAR(50) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, VERIFIED, REJECTED',
    address VARCHAR(512),
    nik VARCHAR(20) UNIQUE COMMENT 'National ID',
    birthdate DATE,
    phone_number VARCHAR(20),
    account_number VARCHAR(50) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_kyc_status (kyc_status),
    INDEX idx_nik (nik)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Employee table (staff and field officers)
CREATE TABLE employees (
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT 'Employee ID',
    role VARCHAR(50) NOT NULL COMMENT 'validator, officer',
    name VARCHAR(255) NOT NULL,
    hire_date DATE NOT NULL,
    city_domicile VARCHAR(100),
    phone_number VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_role (role),
    INDEX idx_hire_date (hire_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Borrower profile (linked to members)
CREATE TABLE borrower_profiles (
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT 'Borrower profile ID',
    member_id INT NOT NULL UNIQUE,
    credit_score INT COMMENT 'Credit score 0-1000',
    monthly_income DECIMAL(15, 2),
    business_category VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
--    CONSTRAINT fk_borrower_member FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
    INDEX idx_credit_score (credit_score),
    INDEX idx_business_category (business_category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Investor profile (linked to members)
CREATE TABLE investor_profiles (
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT 'Investor profile ID',
    member_id INT NOT NULL UNIQUE,
    investment_limit DECIMAL(15, 2),
    risk_appetite_score INT COMMENT 'Risk score 1-10',
    total_balance DECIMAL(19, 2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
--    CONSTRAINT fk_investor_member FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
    INDEX idx_risk_score (risk_appetite_score),
    INDEX idx_total_balance (total_balance)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Loans table (core aggregate root)
CREATE TABLE loans (
    id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT 'Auto-incrementing loan ID',
    borrower_id INT NOT NULL,
    principal_amount DECIMAL(19, 2) NOT NULL,
    rate DECIMAL(5, 2) NOT NULL COMMENT 'Annual interest rate %',
    roi DECIMAL(5, 2) NOT NULL COMMENT 'Annual ROI %',
    status VARCHAR(50) NOT NULL DEFAULT 'PROPOSED' COMMENT 'PROPOSED, APPROVED, INVESTED, DISBURSED',
    agreement_letter_url VARCHAR(512),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
--    CONSTRAINT fk_loan_borrower FOREIGN KEY (borrower_id) REFERENCES members(id) ON DELETE RESTRICT,
    CONSTRAINT chk_principal_positive CHECK (principal_amount > 0),
    CONSTRAINT chk_rate_valid CHECK (rate >= 0),
    CONSTRAINT chk_roi_valid CHECK (roi >= 0),
    INDEX idx_borrower_id (borrower_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Loan approvals (1:1 with Loan)
CREATE TABLE loan_approvals (
    id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT 'Auto-incrementing approval ID',
    loan_id BIGINT NOT NULL UNIQUE,
    field_validator_employee_id INT NOT NULL,
    field_validator_photo_proof_path VARCHAR(512),
    approval_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 --   CONSTRAINT fk_loan_approval FOREIGN KEY (loan_id) REFERENCES loans(id) ON DELETE CASCADE,
 --   CONSTRAINT fk_approval_employee FOREIGN KEY (field_validator_employee_id) REFERENCES employees(id) ON DELETE RESTRICT,
    INDEX idx_employee_id (field_validator_employee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Loan investments (1:N with Loan)
CREATE TABLE loan_investments (
    id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT 'Auto-incrementing investment ID',
    loan_id BIGINT NOT NULL,
    investor_id INT NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    invested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 --   CONSTRAINT fk_loan_investment FOREIGN KEY (loan_id) REFERENCES loans(id) ON DELETE CASCADE,
 --   CONSTRAINT fk_investment_investor FOREIGN KEY (investor_id) REFERENCES members(id) ON DELETE RESTRICT,
    CONSTRAINT chk_investment_positive CHECK (amount > 0),
    INDEX idx_loan_id (loan_id),
    INDEX idx_investor_id (investor_id),
    INDEX idx_invested_at (invested_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Loan disbursements (1:1 with Loan)
CREATE TABLE loan_disbursements (
    id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT 'Auto-incrementing disbursement ID',
    loan_id BIGINT NOT NULL UNIQUE,
    field_officer_employee_id INT NOT NULL,
    signed_agreement_letter_path VARCHAR(512) NOT NULL,
    disbursement_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_loan_disbursement FOREIGN KEY (loan_id) REFERENCES loans(id) ON DELETE CASCADE,
    CONSTRAINT fk_disbursement_employee FOREIGN KEY (field_officer_employee_id) REFERENCES employees(id) ON DELETE RESTRICT,
    INDEX idx_officer_id (field_officer_employee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
