-- Create schemas for each service
CREATE SCHEMA IF NOT EXISTS customer_schema;
CREATE SCHEMA IF NOT EXISTS order_schema;
CREATE SCHEMA IF NOT EXISTS payment_schema;
CREATE SCHEMA IF NOT EXISTS restaurant_schema;


-- Create users for each service
CREATE USER customer_user WITH PASSWORD 'customer_password';
CREATE USER order_user WITH PASSWORD 'order_password';
CREATE USER payment_user WITH PASSWORD 'payment_password';
CREATE USER restaurant_user WITH PASSWORD 'restaurant_password';
CREATE USER admin_user WITH PASSWORD 'admin_password';
ALTER USER admin_user WITH SUPERUSER;

-- Grant privileges
GRANT ALL PRIVILEGES ON SCHEMA customer_schema TO customer_user;
GRANT ALL PRIVILEGES ON SCHEMA order_schema TO order_user;
GRANT ALL PRIVILEGES ON SCHEMA payment_schema TO payment_user;
GRANT ALL PRIVILEGES ON SCHEMA restaurant_schema TO restaurant_user;

-- Allow users to use the schemas
ALTER ROLE customer_user SET search_path TO customer_schema;
ALTER ROLE order_user SET search_path TO order_schema;
ALTER ROLE payment_user SET search_path TO payment_schema;
ALTER ROLE restaurant_user SET search_path TO restaurant_schema;
