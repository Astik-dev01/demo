-- Initialize TaskFlow database schema
CREATE SCHEMA IF NOT EXISTS taskflow;

-- Grant permissions
GRANT ALL PRIVILEGES ON SCHEMA taskflow TO taskflow;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA taskflow TO taskflow;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA taskflow TO taskflow;

-- Set default schema
ALTER USER taskflow SET search_path TO taskflow;
