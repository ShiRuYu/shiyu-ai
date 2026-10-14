#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$repo_root"
container="shiyu-pg-verify-$$"
app_pid=""
log_dir="${RUNNER_TEMP:-/tmp}"
cleanup() {
  if [[ -n "$app_pid" ]]; then
    kill "$app_pid" 2>/dev/null || true
    wait "$app_pid" 2>/dev/null || true
  fi
  docker rm -f "$container" >/dev/null 2>&1 || true
}
trap cleanup EXIT

docker run --rm -d --name "$container" \
  -e POSTGRES_USER=shiyu_verify -e POSTGRES_DB=shiyu_verify \
  -e POSTGRES_HOST_AUTH_METHOD=trust \
  -p 127.0.0.1:55432:5432 \
  -v "$repo_root/scripts/database/postgresql:/migration:ro" \
  postgres:17.11 >/dev/null

ready=false
for _ in $(seq 1 60); do
  if docker exec "$container" pg_isready -U shiyu_verify -d shiyu_verify >/dev/null 2>&1; then
    ready=true
    break
  fi
  sleep 1
done
if [[ "$ready" != true ]]; then
  docker logs "$container"
  exit 1
fi

if docker exec "$container" psql -U shiyu_verify -d shiyu_verify \
    -v ON_ERROR_STOP=1 --single-transaction \
    -f /migration/baseline-schema.sql -c 'SELECT 1/0' \
    >"$log_dir/shiyu-postgresql-rollback.log" 2>&1; then
  echo 'PostgreSQL migration unexpectedly accepted an invalid seed.'
  exit 1
fi
rolled_back="$(docker exec "$container" psql -U shiyu_verify -d shiyu_verify \
  -At -v ON_ERROR_STOP=1 -c "SELECT to_regclass('public.auth_tenant') IS NULL")"
if [[ "$rolled_back" != t ]]; then
  echo 'PostgreSQL migration left schema tables after a failed transaction.'
  exit 1
fi

if ! docker exec "$container" psql -U shiyu_verify -d shiyu_verify \
    -v ON_ERROR_STOP=1 --single-transaction \
    -f /migration/baseline-schema.sql -f /migration/baseline-seed.sql \
    >"$log_dir/shiyu-postgresql-migration.log" 2>&1; then
  tail -n 50 "$log_dir/shiyu-postgresql-migration.log"
  exit 1
fi

export SHIYU_DB_URL='jdbc:postgresql://127.0.0.1:55432/shiyu_verify'
export SHIYU_DB_USERNAME='shiyu_verify'
export SHIYU_DB_PASSWORD=''
export SHIYU_VERIFY_BASE_URL='http://127.0.0.1:19001'
app_home="$(mktemp -d)"
java "-Dapp.home=$app_home" \
  -jar modules/applications/shiyu-ai-bootstrap/target/shiyu-ai-bootstrap-1.0.0.jar \
  --server.port=19001 --spring.profiles.active=dev,ai,postgresql \
  --shiyu.console.enabled=false >"$log_dir/shiyu-postgresql-app.log" 2>&1 &
app_pid=$!

ready=false
for _ in $(seq 1 90); do
  if curl -fsS "$SHIYU_VERIFY_BASE_URL/v3/api-docs" >/dev/null 2>&1; then
    ready=true
    break
  fi
  if ! kill -0 "$app_pid" 2>/dev/null; then break; fi
  sleep 2
done
if [[ "$ready" != true ]]; then
  tail -n 80 "$log_dir/shiyu-postgresql-app.log"
  exit 1
fi
python scripts/database/verify_postgresql_api.py
