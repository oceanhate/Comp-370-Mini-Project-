#!/bin/bash
# simulate_delay.sh — Simulate delayed heartbeats by pausing a server

# You can change this to test other servers
SERVER="HeartBeatServer_1"
DELAY_TIME=10  # seconds to pause

PID=$(pgrep -f "$SERVER")

if [ -z "$PID" ]; then
  echo "  $SERVER is not running."
  exit 1
fi

echo "  Simulating heartbeat delay on $SERVER (PID: $PID)..."
kill -STOP "$PID"

echo " $SERVER paused for $DELAY_TIME seconds..."
sleep "$DELAY_TIME"

echo "  Resuming $SERVER..."
kill -CONT "$PID"

echo " Heartbeat delay simulation complete."
