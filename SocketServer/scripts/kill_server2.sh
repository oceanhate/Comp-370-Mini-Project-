#!/bin/bash
# kill_server2.sh — Stop HeartBeatServer_2

PID=$(pgrep -f "HeartBeatServer_2")
if [ -n "$PID" ]; then
  kill "$PID"
  echo " HeartBeatServer_2 stopped (PID: $PID)"
else
  echo "  HeartBeatServer_2 not running."
fi
