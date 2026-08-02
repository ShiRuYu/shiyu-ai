#!/usr/bin/env bash
set -euo pipefail

repo="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
output="${1:-$repo/dist/cloud-linux}"
cd "$repo"

mvn -Pprod -DskipTests -Djacoco.skip=true clean package
mkdir -p "$output"/data/{db,files,index,models,backups}
cp shiyu-ai-bootstrap/target/shiyu-ai-bootstrap-*.jar "$output/"
cp README.md "$output/"
echo "Cloud Linux package created at $output"
