#!/bin/bash
# Runs once when the postgres container's data directory is first initialized
# (docker-entrypoint-initdb.d convention). Creates a least-privilege role that
# owns the schema and is the ONLY role the app or Flyway ever connects as, so
# the FORCE ROW LEVEL SECURITY policies in V9 actually apply to it — table
# owners are otherwise exempt from RLS.
set -euo pipefail

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE ROLE ${APP_DB_USER} LOGIN PASSWORD '${APP_DB_PASSWORD}';
    ALTER DATABASE ${POSTGRES_DB} OWNER TO ${APP_DB_USER};
    GRANT ALL PRIVILEGES ON DATABASE ${POSTGRES_DB} TO ${APP_DB_USER};
EOSQL
