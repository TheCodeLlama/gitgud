-- Initialize Keycloak database
-- This script creates the keycloak database and grants permissions

-- Create the keycloak database if it doesn't exist
SELECT 'CREATE DATABASE keycloak'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'keycloak')\gexec

-- Grant all privileges on keycloak database to the admin user
GRANT ALL PRIVILEGES ON DATABASE keycloak TO admin;
