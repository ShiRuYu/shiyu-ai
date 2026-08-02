#!/usr/bin/env bash
set -euo pipefail

repo="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
output="${1:-$repo/dist/offline-linux}"
cd "$repo"

mvn -Pprod,offline-models -DskipTests -Djacoco.skip=true clean package
mkdir -p "$output"/data/{db,files,index,models,backups}
cp shiyu-ai-bootstrap/target/shiyu-ai-bootstrap-*.jar "$output/"
cp README.md "$output/"
echo "Offline Linux package created at $output"
