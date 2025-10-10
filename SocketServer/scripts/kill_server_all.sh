#!/bin/bash
# kill_all_servers.sh — Stop all HeartBeat servers

PIDS=$(pgrep -f "HeartBeatServer_")
if [ -n "$PIDS" ]; then
  kill $PIDS
  echo " All HeartBeat servers stopped (PIDs: $PIDS)"
else
  echo "  No HeartBeat servers are running."
fi
