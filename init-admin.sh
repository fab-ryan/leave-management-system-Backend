#!/bin/bash

# Wait for PostgreSQL to be ready
echo "Waiting for PostgreSQL to be ready..."
while ! pg_isready -h localhost -p 5432 -U postgres; do
    sleep 1
done

# Execute the SQL script
echo "Executing admin initialization script..."
psql -h localhost -p 5432 -U postgres -d leave_management -f init-admin.sql

echo "Admin initialization completed!" 