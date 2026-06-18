#!/usr/bin/env bash
set -euo pipefail

HOST="127.0.0.1"

wait_port() {
  local port="$1"
  echo "Waiting for ${HOST}:${port}..."
  for i in {1..60}; do
    if nc -z "${HOST}" "${port}" 2>/dev/null; then
      echo "Port ${port} is up."
      return 0
    fi
    sleep 1
  done
  echo "Timeout waiting for port ${port}"
  exit 1
}

init_rs() {
  local name="$1"
  local port="$2"
  local rs_name="$3"
  local cfg_json="$4"
  local primary_wait="${5:-30}"

  wait_port "$port"
  echo "Checking RS '${rs_name}' at ${HOST}:${port}..."

  CFG_JSON="$cfg_json" PRIMARY_WAIT="$primary_wait" mongosh --host "$HOST" --port "$port" --quiet --file <(cat <<'JS'
try {
  const s = db.adminCommand({ replSetGetStatus: 1 });
  if (s.ok === 1) {
    print("Already initialized: " + s.set);
    quit(0);
  } else {
    throw new Error("Unexpected status: " + tojson(s));
  }
} catch (e) {
  if (e.code === 94) {
    print("Initializing replica set...");
    const cfg = JSON.parse(process.env.CFG_JSON);
    printjson(rs.initiate(cfg));
    for (let i = 0; i < Number(process.env.PRIMARY_WAIT || 30); i++) {
      const hello = (db.hello && db.hello()) || db.isMaster();
      if (hello.isWritablePrimary || hello.ismaster === true) {
        print("PRIMARY is ready: " + (hello.setName || "RS"));
        quit(0);
      }
      sleep(1000);
    }
    throw new Error("Timeout waiting for PRIMARY");
  } else {
    throw e;
  }
}
JS
)
  echo "${name}: ok."
}

CFG_CFG='{
  "_id": "cfgRS",
  "configsvr": true,
  "members": [
    { "_id": 0, "host": "127.0.0.1:27017" },
    { "_id": 1, "host": "127.0.0.1:27018" },
    { "_id": 2, "host": "127.0.0.1:27019" }
  ]
}'

init_rs "cfgRS" 27017 "cfgRS" "$CFG_CFG" 60
echo "cfgRS verified/initialized."