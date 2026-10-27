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
  pgvector/pgvector:pg16 >/dev/null

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
docker exec "$container" psql -U shiyu_verify -d shiyu_verify \
  -v ON_ERROR_STOP=1 -c 'CREATE EXTENSION vector' >/dev/null

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

export SHIYU_DB_URL='jdbc:postgresql://127.0.0.1:55432/shiyu_verify'
export SHIYU_DB_USERNAME='shiyu_verify'
export SHIYU_DB_PASSWORD=''
export SHIYU_VERIFY_BASE_URL='http://127.0.0.1:19001'

# 使用同一个一次性实例验证真实 JDBC 写入与跨 provider 的检索契约。
export SHIYU_PG_TEST_URL="$SHIYU_DB_URL"
export SHIYU_PG_TEST_USERNAME="$SHIYU_DB_USERNAME"
export SHIYU_PG_TEST_PASSWORD="$SHIYU_DB_PASSWORD"
mvn --batch-mode --no-transfer-progress \
  -pl modules/infrastructure/shiyu-common-vector,modules/domains/governance/shiyu-governance-implementation,modules/domains/knowledge/shiyu-knowledge-implementation,modules/domains/agent/shiyu-agent-implementation \
  -am test -Dtest=PgVectorStorePostgresqlTest,QuotaGovernancePostgresqlTest,KnowledgeSpaceMapperPostgresqlTest,AgentVersionPostgresqlTest \
  -Dsurefire.failIfNoSpecifiedTests=false -Ddependency-check.skip=true

start_app() {
  local app_home
  app_home="$(mktemp -d)"
  java "-Dapp.home=$app_home" \
    -jar modules/applications/shiyu-ai-bootstrap/target/shiyu-ai-bootstrap-1.0.0.jar \
    --server.port=19001 --spring.profiles.active=dev,ai,postgresql \
    --mybatis-flex.datasource.agent.url="$SHIYU_DB_URL" \
    --mybatis-flex.datasource.agent.username="$SHIYU_DB_USERNAME" \
    --mybatis-flex.datasource.agent.password="$SHIYU_DB_PASSWORD" \
    --shiyu.vector-store.type="${SHIYU_VECTOR_STORE_TYPE:-jvector}" \
    --shiyu.console.enabled=false >"$log_dir/shiyu-postgresql-app.log" 2>&1 &
  app_pid=$!

  local ready=false
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
    stop_app
    return 1
  fi
}

stop_app() {
  if [[ -n "$app_pid" ]]; then
    kill "$app_pid" 2>/dev/null || true
    wait "$app_pid" 2>/dev/null || true
    app_pid=""
  fi
}

start_app
python scripts/database/verify_postgresql_api.py

docker exec "$container" psql -U shiyu_verify -d shiyu_verify \
  -v ON_ERROR_STOP=1 -c "INSERT INTO common_dict (dict_type, dict_label, dict_value, tenant_id, remark) VALUES ('ci-sentinel', 'preserve', 'persisted', 1, 'startup restart sentinel')" >/dev/null
# 匹配基线的重启必须保留已修改的种子记录和哨兵数据。
docker exec "$container" psql -U shiyu_verify -d shiyu_verify \
  -v ON_ERROR_STOP=1 -c "UPDATE common_dict SET remark='customized by PostgreSQL CI' WHERE id=1" >/dev/null
stop_app
start_app
sentinel_count="$(docker exec "$container" psql -U shiyu_verify -d shiyu_verify \
  -At -v ON_ERROR_STOP=1 -c "SELECT COUNT(*) FROM common_dict WHERE dict_type='ci-sentinel' AND dict_value='persisted' AND remark='startup restart sentinel'")"
custom_seed_count="$(docker exec "$container" psql -U shiyu_verify -d shiyu_verify \
  -At -v ON_ERROR_STOP=1 -c "SELECT COUNT(*) FROM common_dict WHERE id=1 AND remark='customized by PostgreSQL CI'")"
if [[ "$sentinel_count" != 1 || "$custom_seed_count" != 1 ]]; then
  echo 'PostgreSQL restart did not preserve the sentinel or customized seed row.'
  exit 1
fi
python scripts/database/verify_postgresql_api.py
stop_app

# 版本标记不匹配时必须拒绝启动，并保留应用记录。
docker exec "$container" psql -U shiyu_verify -d shiyu_verify \
  -v ON_ERROR_STOP=1 -c "UPDATE common_schema_baseline SET baseline_version='3' WHERE id=1" >/dev/null
if start_app; then
  echo 'PostgreSQL unexpectedly accepted an unsupported baseline marker.'
  exit 1
fi
custom_seed_count="$(docker exec "$container" psql -U shiyu_verify -d shiyu_verify \
  -At -v ON_ERROR_STOP=1 -c "SELECT COUNT(*) FROM common_dict WHERE id=1 AND remark='customized by PostgreSQL CI'")"
if [[ "$custom_seed_count" != 1 ]]; then
  echo 'Refusing a mismatched marker changed existing seed data.'
  exit 1
fi
stop_app

# 验证非空且无标记的数据库会拒绝初始化，并保留已有内容。
docker exec "$container" psql -U shiyu_verify -d shiyu_verify \
  -v ON_ERROR_STOP=1 -c 'DROP SCHEMA public CASCADE; CREATE SCHEMA public; CREATE EXTENSION vector' >/dev/null
# 通过其他模式内的对象确认初始化器仅检查 public 模式中的应用表。
docker exec "$container" psql -U shiyu_verify -d shiyu_verify \
  -v ON_ERROR_STOP=1 -c 'CREATE TABLE public.unmarked_sentinel (id integer PRIMARY KEY); INSERT INTO public.unmarked_sentinel VALUES (7)' >/dev/null
if start_app; then
  echo 'PostgreSQL unexpectedly accepted a non-empty unmarked database.'
  exit 1
fi
unmarked_count="$(docker exec "$container" psql -U shiyu_verify -d shiyu_verify \
  -At -v ON_ERROR_STOP=1 -c 'SELECT COUNT(*) FROM public.unmarked_sentinel WHERE id=7')"
if [[ "$unmarked_count" != 1 ]]; then
  echo 'Refusing an unmarked database changed its sentinel.'
  exit 1
fi
stop_app

printf '%s\n' 'PostgreSQL empty-database initialization, restart preservation, refusal and API reads passed.'
docker exec "$container" psql -U shiyu_verify -d shiyu_verify \
  -v ON_ERROR_STOP=1 -c 'DROP SCHEMA public CASCADE; CREATE SCHEMA public; CREATE EXTENSION vector' >/dev/null
export SHIYU_VECTOR_STORE_TYPE=pgvector
start_app
vector_extension="$(docker exec "$container" psql -U shiyu_verify -d shiyu_verify \
  -At -v ON_ERROR_STOP=1 -c "SELECT EXISTS (SELECT 1 FROM pg_extension WHERE extname='vector')")"
vector_table="$(docker exec "$container" psql -U shiyu_verify -d shiyu_verify \
  -At -v ON_ERROR_STOP=1 -c "SELECT to_regclass('public.shiyu_vector_item') IS NOT NULL")"
if [[ "$vector_extension" != t || "$vector_table" != t ]]; then
  echo 'pgvector provider did not initialize its vector table.'
  exit 1
fi
printf '%s\n' 'PostgreSQL pgvector provider initialization passed.'
