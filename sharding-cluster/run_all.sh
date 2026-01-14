#!/usr/bin/env bash
set -euo pipefail

DIR="$(cd "$(dirname "$0")" && pwd)"
DO_CLEAN="yes"
[[ "${1:-}" == "--no-clean" ]] && DO_CLEAN="no"

step() { echo -e "\n=== $1 ==="; }
run() { step "$1"; bash "$2"; }

if [[ "$DO_CLEAN" == "yes" ]]; then
  run "00 stop_and_clean"       "$DIR/00 - stop_and_clean.sh"
else
  echo "Skipping 00 - stop_and_clean.sh"
fi

run "01 create_dir"              "$DIR/01 - create_dir.sh"
run "02 start_mongod_processes"  "$DIR/02 - start_mongod_processes.sh"
run "03 start_config_server"     "$DIR/03 - start_config_server.sh"
run "04 start_rpls_shard"        "$DIR/04 - start_rpls_shard.sh"
run "05 start_mongos_shards"     "$DIR/05 - start_mongos_shards.sh"

echo -e "\n✅ All done."
