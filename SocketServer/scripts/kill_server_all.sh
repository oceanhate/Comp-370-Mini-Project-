#!/bin/bash
# kill_all_servers.sh — Works on Windows (Git Bash)

PIDS=$(ps -ef | grep "HeartBeatServer_" | grep -v grep | awk '{print $2}')

if [ -n "$PIDS" ]; then
  echo " Stopping all HeartBeat servers..."
  kill $PIDS
  echo " All servers stopped (PIDs: $PIDS)"
else
  echo "  No HeartBeat servers are running."
fi
