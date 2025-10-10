#!/bin/bash
# run.sh — Compile and run the full Heartbeat Monitor system
# Author: Mehul Singla
# Project: Comp-370 Mini Project

SRC_DIR="Comp-370-Mini-Project-/Heartbeat Servers and Monitor/src"
OUT_DIR="out"

# Step 1: Compile all source files
echo " Compiling Java source files..."
mkdir -p "$OUT_DIR"
javac -d "$OUT_DIR" "$SRC_DIR"/*.java

if [ $? -ne 0 ]; then
  echo " Compilation failed. Please fix errors above."
  exit 1
fi
echo " Compilation successful."

# Step 2: Run Heartbeat servers in background
echo " Starting Heartbeat servers..."
java -cp "$OUT_DIR" HeartBeatServer_1 &
SERVER1_PID=$!
java -cp "$OUT_DIR" HeartBeatServer_2 &
SERVER2_PID=$!
java -cp "$OUT_DIR" HeartBeatServer_3 &
SERVER3_PID=$!
sleep 2

echo " Servers started (PIDs: $SERVER1_PID, $SERVER2_PID, $SERVER3_PID)"

# Step 3: Start Heartbeat Monitor
echo " Starting Heartbeat Monitor..."
java -cp "$OUT_DIR" HeartBeatMonitor


