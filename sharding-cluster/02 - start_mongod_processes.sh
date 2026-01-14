#!/usr/bin/env bash
set -euo pipefail

BASE_DIR="/Users/ricardomello/Documents/projects/MongoDB"
MONGOD_BIN="${MONGOD_BIN:-mongod}"

start_instance() {
  local name="$1"
  local role="$2"
  local replset="$3"
  local dbpath="$4"
  local port="$5"
  local logpath="$6"

  mkdir -p "$dbpath" "$(dirname "$logpath")"
  touch "$logpath"

  if lsof -iTCP:"$port" -sTCP:LISTEN -nP >/dev/null 2>&1; then
    echo "Port $port already in use. Skipping $name."
    return 0
  fi

  echo "Starting $name (rs=$replset, $role, port=$port)"
  "$MONGOD_BIN" \
    --"$role" \
    --replSet "$replset" \
    --dbpath "$dbpath" \
    --port "$port" \
    --bind_ip "127.0.0.1" \
    --fork \
    --logpath "$logpath"

  echo "$name started. Log: $logpath"
}

start_instance "cfg27017" "configsvr" "cfgRS" "$BASE_DIR/configdb/cfg27017" 27017 "$BASE_DIR/logs/cfg27017.log"
start_instance "cfg27018" "configsvr" "cfgRS" "$BASE_DIR/configdb/cfg27018" 27018 "$BASE_DIR/logs/cfg27018.log"
start_instance "cfg27019" "configsvr" "cfgRS" "$BASE_DIR/configdb/cfg27019" 27019 "$BASE_DIR/logs/cfg27019.log"

start_instance "sh27020" "shardsvr" "shardRS1" "$BASE_DIR/shard1/sh27020" 27020 "$BASE_DIR/logs/sh27020.log"
start_instance "sh27021" "shardsvr" "shardRS1" "$BASE_DIR/shard1/sh27021" 27021 "$BASE_DIR/logs/sh27021.log"
start_instance "sh27022" "shardsvr" "shardRS1" "$BASE_DIR/shard1/sh27022" 27022 "$BASE_DIR/logs/sh27022.log"

start_instance "sh27023" "shardsvr" "shardRS2" "$BASE_DIR/shard2/sh27023" 27023 "$BASE_DIR/logs/sh27023.log"
start_instance "sh27024" "shardsvr" "shardRS2" "$BASE_DIR/shard2/sh27024" 27024 "$BASE_DIR/logs/sh27024.log"
start_instance "sh27025" "shardsvr" "shardRS2" "$BASE_DIR/shard2/sh27025" 27025 "$BASE_DIR/logs/sh27025.log"

echo
echo "Listening processes:"
lsof -nP -iTCP -sTCP:LISTEN | grep -E ":(2701[7-9]|2702[0-5])\b" || true
echo "Done."