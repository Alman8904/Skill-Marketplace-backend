-- Add wallet balance to user_model
ALTER TABLE user_model
ADD COLUMN wallet_balance DOUBLE PRECISION DEFAULT 0.0 NOT NULL;

-- Add estimated hours and deadline to orders
ALTER TABLE orders
ADD COLUMN estimated_hours INTEGER;

ALTER TABLE orders
ADD COLUMN deadline TIMESTAMP;
