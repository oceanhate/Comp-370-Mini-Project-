#!/bin/bash
# simulate_recovery.sh — Restart one or more servers

OUT_DIR="out"
echo " Simulating server recovery..."

# List of servers to restart
SERVERS=("HeartBeatServer_1" "HeartBeatServer_2")

for SERVER in "${SERVERS[@]}"; do
  RUNNING=$(pgrep -f "$SERVER")
  if [ -z "$RUNNING" ]; then
    java -cp "$OUT_DIR" "$SERVER" &
    echo " $SERVER restarted successfully."
  else
    echo " $SERVER is already running."
  fi
done

echo " Recovery simulation complete."
