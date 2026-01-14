#!/usr/bin/env bash
set -euo pipefail

BASE_DIR="/Users/ricardomello/Documents/projects/MongoDB"

DIRS=(
  "$BASE_DIR/configdb/cfg27017"
  "$BASE_DIR/configdb/cfg27018"
  "$BASE_DIR/configdb/cfg27019"
  "$BASE_DIR/shard1/sh27020"
  "$BASE_DIR/shard1/sh27021"
  "$BASE_DIR/shard1/sh27022"
  "$BASE_DIR/shard2/sh27023"
  "$BASE_DIR/shard2/sh27024"
  "$BASE_DIR/shard2/sh27025"
  "$BASE_DIR/logs"
)

echo "Creating directory structure at: $BASE_DIR"
for d in "${DIRS[@]}"; do
  mkdir -p "$d"
done

chmod -R 700 "$BASE_DIR/configdb" "$BASE_DIR/shard1" "$BASE_DIR/shard2" || true
chmod 755 "$BASE_DIR" "$BASE_DIR/logs" || true

touch "$BASE_DIR/logs/.keep"

echo "Structure created. Preview:"
find "$BASE_DIR" -maxdepth 3 -print