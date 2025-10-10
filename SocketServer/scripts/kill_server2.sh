#!/bin/bash
# kill_server2.sh — Stop HeartBeatServer_2 (Git Bash compatible)

PID=$(ps -ef | grep "HeartBeatServer_2" | grep -v grep | awk '{print $2}')

if [ -n "$PID" ]; then
  kill "$PID"
  echo " HeartBeatServer_2 stopped (PID: $PID)"
else
  echo "⚠  HeartBeatServer_2 not running."
fi
