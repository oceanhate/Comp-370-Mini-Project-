#!/bin/bash
# kill_server2.sh — Stop HeartBeatServer_2 (Git Bash compatible)

PID=$(ps -ef | grep "HeartBeatServer_3" | grep -v grep | awk '{print $2}')

if [ -n "$PID" ]; then
  kill "$PID"
  echo " HeartBeatServer_3 stopped (PID: $PID)"
else
  echo " HeartBeatServer_3 not running."
fi
