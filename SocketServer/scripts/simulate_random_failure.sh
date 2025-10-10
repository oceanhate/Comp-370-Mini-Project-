#!/bin/bash
# simulate_random_failure.sh — Randomly kill one running server

SERVERS=("HeartBeatServer_1" "HeartBeatServer_2" "HeartBeatServer_3")

# Pick one at random
RANDOM_SERVER=${SERVERS[$RANDOM % ${#SERVERS[@]}]}

PID=$(pgrep -f "$RANDOM_SERVER")
if [ -n "$PID" ]; then
  kill "$PID"
  echo " Random failure simulated: $RANDOM_SERVER stopped (PID: $PID)"
else
  echo "  $RANDOM_SERVER not running, no action taken."
fi
