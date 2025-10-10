#!/bin/bash
# kill_server1.sh — Stop HeartBeatServer_1

PID=$(pgrep -f "HeartBeatServer_1")
if [ -n "$PID" ]; then
  kill "$PID"
  echo "HeartBeatServer_1 stopped (PID: $PID)"
else
  echo "  HeartBeatServer_1 not running."
fi
