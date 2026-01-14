#!/usr/bin/env bash
set -euo pipefail

BASE_DIR="/Users/ricardomello/Documents/projects/MongoDB"
HOST="127.0.0.1"
PORTS=(27017 27018 27019 27020 27021 27022 27023 27024 27025 28000)

echo "Base: $BASE_DIR"
if [[ ! -d "$BASE_DIR" ]]; then
  echo "Base directory does not exist: $BASE_DIR (proceeding to kill processes anyway)"
fi

stop_port() {
  local port="$1"
  local pids
  pids="$(lsof -t -nP -iTCP:"$port" -sTCP:LISTEN || true)"
  if [[ -z "$pids" ]]; then
    echo "Nothing listening on port $port"
    return 0
  fi

  echo "Stopping processes on port $port (PIDs: $pids)"
  kill $pids 2>/dev/null || true

  for i in {1..20}; do
    if ! lsof -nP -iTCP:"$port" -sTCP:LISTEN >/dev/null 2>&1; then
      echo "Port $port freed"
      return 0
    fi
    sleep 0.5
  done

  echo "Force killing port $port"
  kill -9 $pids 2>/dev/null || true
  if lsof -nP -iTCP:"$port" -sTCP:LISTEN >/dev/null 2>&1; then
    echo "Still something on port $port. Please check manually."
  else
    echo "Port $port freed (forced)"
  fi
}

echo "Stopping mongod/mongos"
for p in "${PORTS[@]}"; do
  stop_port "$p"
done

echo
echo "Deleting directories"
DIRS_TO_REMOVE=( "$BASE_DIR/configdb" "$BASE_DIR/shard1" "$BASE_DIR/shard2" "$BASE_DIR/logs" )

for d in "${DIRS_TO_REMOVE[@]}"; do
  if [[ -n "$BASE_DIR" && "$d" == "$BASE_DIR/"* || "$d" == "$BASE_DIR"/* ]]; then
    if [[ -d "$d" ]]; then
      echo "Removing: $d"
      rm -rf "$d"
    else
      echo "Does not exist: $d (ok)"
    fi
  else
    echo "Unsafe path detected: $d — aborting."
    exit 1
  fi
done

if [[ -d "$BASE_DIR" && -z "$(ls -A "$BASE_DIR" 2>/dev/null)" ]]; then
  echo "$BASE_DIR is empty. Removing base directory."
  rmdir "$BASE_DIR" || true
fi

echo "Done."