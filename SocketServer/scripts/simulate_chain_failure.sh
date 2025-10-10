#!/bin/bash
# simulate_chain_failure.sh — Sequentially stop servers to simulate cascading failures

SERVERS=("HeartBeatServer_1" "HeartBeatServer_2" "HeartBeatServer_3")

echo " Starting chain failure simulation..."
for SERVER in "${SERVERS[@]}"; do
  PID=$(pgrep -f "$SERVER")
  if [ -n "$PID" ]; then
    kill "$PID"
    echo " $SERVER stopped (PID: $PID)"
    sleep 3
  else
    echo "  $SERVER is not running."
  fi
done
echo " Chain failure simulation complete."
