#!/bin/bash
# kill_server3.sh — Stop HeartBeatServer_3

PID=$(pgrep -f "HeartBeatServer_3")
if [ -n "$PID" ]; then
  kill "$PID"
  echo " HeartBeatServer_3 stopped (PID: $PID)"
else
  echo "  HeartBeatServer_3 not running."
fi
