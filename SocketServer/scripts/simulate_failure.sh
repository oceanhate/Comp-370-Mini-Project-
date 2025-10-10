#!/bin/bash
# simulate_failure.sh — Simulate one or more server failures

echo " Simulating server failure..."

# Choose which servers to kill (you can modify this line)
SERVERS=("HeartBeatServer_1" "HeartBeatServer_2")

for SERVER in "${SERVERS[@]}"; do
 PID=$(ps -ef | grep "$SERVER" | grep -v grep | awk '{print $2}')
  if [ -n "$PID" ]; then
    kill "$PID"
    echo "$SERVER stopped (PID: $PID)"
  else
    echo " $SERVER is not running."
  fi
done

echo " Failure simulation complete."
