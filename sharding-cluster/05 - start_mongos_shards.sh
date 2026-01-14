#!/usr/bin/env bash
set -euo pipefail

BASE_DIR="/Users/ricardomello/Documents/projects/MongoDB"

HOST="127.0.0.1"
PORT=28000
CFGRS="cfgRS/127.0.0.1:27017,127.0.0.1:27018,127.0.0.1:27019"
LOG="$BASE_DIR/logs/mongos-${PORT}.log"

MONGOS_BIN="${MONGOS_BIN:-mongos}"
MONGOSH_BIN="${MONGOSH_BIN:-mongosh}"

wait_port() {
  local port="$1"
  echo "Waiting for ${HOST}:${port}..."
  for i in {1..60}; do
    if nc -z "$HOST" "$port" 2>/dev/null; then
      echo "Port ${port} is up."
      return 0
    fi
    sleep 1
  done
  echo "Timeout waiting for port ${port}"
  exit 1
}

start_mongos() {
  mkdir -p "$(dirname "$LOG")"
  touch "$LOG"

  if lsof -nP -iTCP:"$PORT" -sTCP:LISTEN >/dev/null 2>&1; then
    echo "mongos already listening on :$PORT. Skipping start."
    return 0
  fi

  echo "Starting mongos on :$PORT (configdb=$CFGRS)"
  "$MONGOS_BIN" \
    --configdb "$CFGRS" \
    --bind_ip "$HOST" \
    --port "$PORT" \
    --fork \
    --logpath "$LOG"

  echo "Log: $LOG"
}

add_shards_and_status() {
  wait_port "$PORT"
  echo "Adding shards on mongos :$PORT (idempotent)..."

  "$MONGOSH_BIN" --host "$HOST" --port "$PORT" --quiet --eval '
    function ensureShard(connStr) {
      const list = db.adminCommand({ listShards: 1 });
      if (list.ok !== 1) {
        throw new Error("listShards failed: " + tojson(list));
      }
      const exists = (list.shards || []).some(s => s.host.startsWith(connStr.split("/")[0] + "/"));
      if (exists) {
        print("Already exists: " + connStr);
        return;
      }
      const res = sh.addShard(connStr);
      if (res.ok !== 1) {
        throw new Error("addShard failed: " + tojson(res));
      }
      print("Added: " + connStr);
    }

    ensureShard("shardRS1/127.0.0.1:27020,127.0.0.1:27021,127.0.0.1:27022");
    ensureShard("shardRS2/127.0.0.1:27023,127.0.0.1:27024,127.0.0.1:27025");

    print("\n=== sh.status() ===");
    sh.status();
  '
}

start_mongos
add_shards_and_status
echo "mongos ready on :$PORT and shards verified."