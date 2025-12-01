-- ============================================================================
-- V1__Initial_Schema.sql
-- FinTech Mobile Banking Application - Initial Database Schema
-- ============================================================================

-- Create schema
CREATE SCHEMA IF NOT EXISTS banking;

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- USERS TABLE
-- ============================================================================
CREATE TABLE banking.users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(15) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    date_of_birth DATE,
    pan_number VARCHAR(10) UNIQUE,
    aadhaar_number VARCHAR(12) UNIQUE,
    kyc_status VARCHAR(20) DEFAULT 'PENDING',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    mfa_enabled BOOLEAN DEFAULT TRUE,
    mfa_secret VARCHAR(255),
    last_login_at TIMESTAMP WITH TIME ZONE,
    login_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    
    CONSTRAINT chk_kyc_status CHECK (kyc_status IN ('PENDING', 'VERIFIED', 'REJECTED', 'EXPIRED')),
    CONSTRAINT chk_user_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'BLOCKED'))
);

CREATE INDEX idx_users_email ON banking.users(email);
CREATE INDEX idx_users_phone ON banking.users(phone_number);
CREATE INDEX idx_users_customer_id ON banking.users(customer_id);

-- ============================================================================
-- ROLES TABLE
-- ============================================================================
CREATE TABLE banking.roles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200)
);

INSERT INTO banking.roles (name, description) VALUES
    ('ROLE_CUSTOMER', 'Regular bank customer'),
    ('ROLE_ADMIN', 'System administrator'),
    ('ROLE_SUPPORT', 'Customer support staff');

-- ============================================================================
-- USER_ROLES TABLE (Many-to-Many)
-- ============================================================================
CREATE TABLE banking.user_roles (
    user_id UUID NOT NULL REFERENCES banking.users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES banking.roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE INDEX idx_user_roles_user ON banking.user_roles(user_id);

-- ============================================================================
-- ACCOUNTS TABLE
-- ============================================================================
CREATE TABLE banking.accounts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    account_number VARCHAR(20) NOT NULL UNIQUE,
    account_type VARCHAR(30) NOT NULL,
    balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    ifsc_code VARCHAR(11) NOT NULL,
    branch_name VARCHAR(100),
    daily_limit DECIMAL(15, 2),
    per_transaction_limit DECIMAL(15, 2),
    interest_rate DECIMAL(5, 2),
    minimum_balance DECIMAL(15, 2),
    user_id UUID NOT NULL REFERENCES banking.users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_account_type CHECK (account_type IN ('SAVINGS', 'CURRENT', 'SALARY', 'FIXED_DEPOSIT', 'RECURRING_DEPOSIT', 'NRI_NRE', 'NRI_NRO', 'LOAN')),
    CONSTRAINT chk_account_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'DORMANT', 'FROZEN', 'CLOSED')),
    CONSTRAINT chk_balance_positive CHECK (balance >= 0)
);

CREATE INDEX idx_accounts_user ON banking.accounts(user_id);
CREATE INDEX idx_accounts_number ON banking.accounts(account_number);
CREATE INDEX idx_accounts_status ON banking.accounts(status);

-- ============================================================================
-- TRANSACTIONS TABLE
-- ============================================================================
CREATE TABLE banking.transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    reference_number VARCHAR(30) NOT NULL UNIQUE,
    transaction_type VARCHAR(30) NOT NULL,
    transaction_mode VARCHAR(20) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    description VARCHAR(500),
    remarks VARCHAR(200),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    from_account_id UUID REFERENCES banking.accounts(id),
    to_account_id UUID REFERENCES banking.accounts(id),
    beneficiary_name VARCHAR(100),
    beneficiary_account VARCHAR(20),
    beneficiary_ifsc VARCHAR(11),
    beneficiary_bank VARCHAR(100),
    balance_after DECIMAL(15, 2),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP WITH TIME ZONE,
    ip_address VARCHAR(45),
    device_id VARCHAR(100),
    user_agent VARCHAR(500),
    location VARCHAR(200),
    failure_reason VARCHAR(500),
    
    CONSTRAINT chk_transaction_type CHECK (transaction_type IN ('CREDIT', 'DEBIT', 'TRANSFER', 'REVERSAL', 'REFUND')),
    CONSTRAINT chk_transaction_mode CHECK (transaction_mode IN ('IMPS', 'NEFT', 'RTGS', 'UPI', 'INTERNAL', 'ATM', 'POS', 'ONLINE', 'MOBILE')),
    CONSTRAINT chk_transaction_status CHECK (status IN ('PENDING', 'PROCESSING', 'SUCCESS', 'FAILED', 'REVERSED', 'CANCELLED')),
    CONSTRAINT chk_amount_positive CHECK (amount > 0)
);

CREATE INDEX idx_transactions_from_account ON banking.transactions(from_account_id);
CREATE INDEX idx_transactions_to_account ON banking.transactions(to_account_id);
CREATE INDEX idx_transactions_reference ON banking.transactions(reference_number);
CREATE INDEX idx_transactions_created ON banking.transactions(created_at);
CREATE INDEX idx_transactions_status ON banking.transactions(status);

-- ============================================================================
-- BENEFICIARIES TABLE
-- ============================================================================
CREATE TABLE banking.beneficiaries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nickname VARCHAR(100) NOT NULL,
    account_number VARCHAR(20) NOT NULL,
    account_holder_name VARCHAR(100) NOT NULL,
    ifsc_code VARCHAR(11) NOT NULL,
    bank_name VARCHAR(100) NOT NULL,
    branch_name VARCHAR(100),
    account_type VARCHAR(20),
    transfer_limit DECIMAL(15, 2),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    is_verified BOOLEAN DEFAULT FALSE,
    verified_at TIMESTAMP WITH TIME ZONE,
    user_id UUID NOT NULL REFERENCES banking.users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_beneficiary_status CHECK (status IN ('PENDING', 'ACTIVE', 'INACTIVE', 'BLOCKED')),
    CONSTRAINT uq_user_beneficiary_account UNIQUE (user_id, account_number)
);

CREATE INDEX idx_beneficiaries_user ON banking.beneficiaries(user_id);
CREATE INDEX idx_beneficiaries_status ON banking.beneficiaries(status);

-- ============================================================================
-- CARDS TABLE
-- ============================================================================
CREATE TABLE banking.cards (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    card_number_masked VARCHAR(20) NOT NULL,
    card_number_encrypted TEXT NOT NULL,
    card_type VARCHAR(20) NOT NULL,
    card_network VARCHAR(20) NOT NULL,
    cardholder_name VARCHAR(100) NOT NULL,
    expiry_date DATE NOT NULL,
    cvv_encrypted TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    daily_limit DECIMAL(15, 2),
    monthly_limit DECIMAL(15, 2),
    credit_limit DECIMAL(15, 2),
    available_credit DECIMAL(15, 2),
    outstanding_amount DECIMAL(15, 2) DEFAULT 0.00,
    international_enabled BOOLEAN DEFAULT FALSE,
    online_enabled BOOLEAN DEFAULT TRUE,
    contactless_enabled BOOLEAN DEFAULT TRUE,
    account_id UUID NOT NULL REFERENCES banking.accounts(id),
    user_id UUID NOT NULL REFERENCES banking.users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_card_type CHECK (card_type IN ('DEBIT', 'CREDIT', 'PREPAID', 'FOREX')),
    CONSTRAINT chk_card_network CHECK (card_network IN ('VISA', 'MASTERCARD', 'RUPAY', 'AMEX', 'DINERS')),
    CONSTRAINT chk_card_status CHECK (status IN ('ACTIVE', 'BLOCKED', 'FROZEN', 'EXPIRED', 'CANCELLED', 'LOST', 'STOLEN'))
);

CREATE INDEX idx_cards_user ON banking.cards(user_id);
CREATE INDEX idx_cards_account ON banking.cards(account_id);
CREATE INDEX idx_cards_status ON banking.cards(status);

-- ============================================================================
-- AUDIT_LOGS TABLE
-- ============================================================================
CREATE TABLE banking.audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(20) NOT NULL,
    old_value JSONB,
    new_value JSONB,
    user_id UUID REFERENCES banking.users(id),
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_entity ON banking.audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_user ON banking.audit_logs(user_id);
CREATE INDEX idx_audit_logs_created ON banking.audit_logs(created_at);

-- ============================================================================
-- OTP_TOKENS TABLE
-- ============================================================================
CREATE TABLE banking.otp_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES banking.users(id),
    otp_hash VARCHAR(255) NOT NULL,
    purpose VARCHAR(50) NOT NULL,
    reference_id VARCHAR(100),
    attempts INTEGER DEFAULT 0,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    verified_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_otp_purpose CHECK (purpose IN ('LOGIN', 'TRANSACTION', 'PASSWORD_RESET', 'BENEFICIARY_ADD', 'CARD_PIN'))
);

CREATE INDEX idx_otp_tokens_user ON banking.otp_tokens(user_id);
CREATE INDEX idx_otp_tokens_expires ON banking.otp_tokens(expires_at);

-- ============================================================================
-- NOTIFICATIONS TABLE
-- ============================================================================
CREATE TABLE banking.notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES banking.users(id),
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(30) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    reference_type VARCHAR(50),
    reference_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP WITH TIME ZONE,
    
    CONSTRAINT chk_notification_type CHECK (type IN ('TRANSACTION', 'SECURITY', 'PROMOTION', 'ALERT', 'INFO'))
);

CREATE INDEX idx_notifications_user ON banking.notifications(user_id);
CREATE INDEX idx_notifications_read ON banking.notifications(user_id, is_read);
CREATE INDEX idx_notifications_created ON banking.notifications(created_at);

-- ============================================================================
-- REFRESH_TOKENS TABLE
-- ============================================================================
CREATE TABLE banking.refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES banking.users(id),
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    device_id VARCHAR(100),
    device_info VARCHAR(200),
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN DEFAULT FALSE,
    revoked_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refresh_tokens_user ON banking.refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires ON banking.refresh_tokens(expires_at);

-- ============================================================================
-- TRIGGER: Update updated_at timestamp
-- ============================================================================
CREATE OR REPLACE FUNCTION banking.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON banking.users
    FOR EACH ROW
    EXECUTE FUNCTION banking.update_updated_at_column();

CREATE TRIGGER update_accounts_updated_at
    BEFORE UPDATE ON banking.accounts
    FOR EACH ROW
    EXECUTE FUNCTION banking.update_updated_at_column();

CREATE TRIGGER update_beneficiaries_updated_at
    BEFORE UPDATE ON banking.beneficiaries
    FOR EACH ROW
    EXECUTE FUNCTION banking.update_updated_at_column();

CREATE TRIGGER update_cards_updated_at
    BEFORE UPDATE ON banking.cards
    FOR EACH ROW
    EXECUTE FUNCTION banking.update_updated_at_column();
