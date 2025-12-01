-- V2__update_billing_schema.sql
-- Update billing schema to match entity definitions

-- Update subscription_status enum
ALTER TYPE subscription_status ADD VALUE IF NOT EXISTS 'TRIALING';
ALTER TYPE subscription_status ADD VALUE IF NOT EXISTS 'PENDING_CANCELLATION';

-- Update payment_status enum for invoices
DROP TYPE IF EXISTS invoice_status CASCADE;
CREATE TYPE invoice_status AS ENUM ('DRAFT', 'PENDING', 'PAID', 'VOID', 'UNCOLLECTIBLE', 'FAILED', 'REFUNDED');

-- Create payment_status_new for payments
DROP TYPE IF EXISTS payment_status_new CASCADE;
CREATE TYPE payment_status_new AS ENUM ('PENDING', 'PROCESSING', 'SUCCEEDED', 'FAILED', 'CANCELLED', 'REFUNDED', 'PARTIALLY_REFUNDED');

-- Create billing_interval enum
CREATE TYPE billing_interval AS ENUM ('MONTHLY', 'QUARTERLY', 'YEARLY');

-- Update subscription_plans table
ALTER TABLE subscription_plans 
    ADD COLUMN IF NOT EXISTS billing_interval billing_interval DEFAULT 'MONTHLY',
    ADD COLUMN IF NOT EXISTS trial_days INTEGER DEFAULT 0;

-- Drop old billing_period column if exists
ALTER TABLE subscription_plans DROP COLUMN IF EXISTS billing_period;

-- Update subscriptions table
ALTER TABLE subscriptions 
    ADD COLUMN IF NOT EXISTS start_date TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS end_date TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS trial_end_date TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS next_billing_date TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS auto_renew BOOLEAN DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS current_period_amount DECIMAL(10, 2),
    ADD COLUMN IF NOT EXISTS currency VARCHAR(3) DEFAULT 'USD';

-- Migrate data from old columns to new columns
UPDATE subscriptions 
SET start_date = current_period_start,
    end_date = current_period_end,
    trial_end_date = trial_end,
    next_billing_date = current_period_end
WHERE start_date IS NULL;

-- Update payment_methods table
ALTER TABLE payment_methods 
    ADD COLUMN IF NOT EXISTS last4 VARCHAR(4),
    ADD COLUMN IF NOT EXISTS card_brand VARCHAR(50),
    ADD COLUMN IF NOT EXISTS expiry_month INTEGER,
    ADD COLUMN IF NOT EXISTS expiry_year INTEGER,
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS billing_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS billing_email VARCHAR(255),
    ADD COLUMN IF NOT EXISTS address_line1 VARCHAR(255),
    ADD COLUMN IF NOT EXISTS address_line2 VARCHAR(255),
    ADD COLUMN IF NOT EXISTS city VARCHAR(100),
    ADD COLUMN IF NOT EXISTS state VARCHAR(100),
    ADD COLUMN IF NOT EXISTS postal_code VARCHAR(20),
    ADD COLUMN IF NOT EXISTS country VARCHAR(3);

-- Migrate data from old columns to new columns
UPDATE payment_methods 
SET last4 = last_four,
    card_brand = brand,
    expiry_month = exp_month,
    expiry_year = exp_year
WHERE last4 IS NULL AND last_four IS NOT NULL;

-- Update invoices table
ALTER TABLE invoices 
    ADD COLUMN IF NOT EXISTS discount DECIMAL(10, 2) DEFAULT 0,
    ADD COLUMN IF NOT EXISTS issued_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS period_start VARCHAR(50),
    ADD COLUMN IF NOT EXISTS period_end VARCHAR(50),
    ADD COLUMN IF NOT EXISTS invoice_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS pdf_url VARCHAR(500);

-- Migrate invoice status
ALTER TABLE invoices ALTER COLUMN status TYPE VARCHAR(50);

-- Update payments table
ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS payment_intent_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS charge_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS refunded_amount DECIMAL(10, 2),
    ADD COLUMN IF NOT EXISTS processed_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS receipt_url VARCHAR(500);

-- Migrate data from old columns to new columns
UPDATE payments 
SET payment_intent_id = stripe_payment_intent_id,
    charge_id = stripe_charge_id,
    refunded_amount = refund_amount
WHERE payment_intent_id IS NULL AND stripe_payment_intent_id IS NOT NULL;

-- Update payments status column
ALTER TABLE payments ALTER COLUMN status TYPE VARCHAR(50);

-- Create indexes for new columns
CREATE INDEX IF NOT EXISTS idx_subscriptions_start_date ON subscriptions(start_date);
CREATE INDEX IF NOT EXISTS idx_subscriptions_end_date ON subscriptions(end_date);
CREATE INDEX IF NOT EXISTS idx_subscriptions_next_billing ON subscriptions(next_billing_date);
CREATE INDEX IF NOT EXISTS idx_payment_methods_active ON payment_methods(user_id, is_active);
CREATE INDEX IF NOT EXISTS idx_invoices_issued_at ON invoices(issued_at);
CREATE INDEX IF NOT EXISTS idx_payments_intent ON payments(payment_intent_id);
