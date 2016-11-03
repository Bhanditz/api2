#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE USER europeana WITH PASSWORD 'culture';
    CREATE DATABASE europeana;
    GRANT ALL PRIVILEGES ON DATABASE europeana TO europeana;
EOSQL