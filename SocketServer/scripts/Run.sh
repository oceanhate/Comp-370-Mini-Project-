#!/bin/bash
# run.sh — Compile and run the full Heartbeat Monitor system
# Author: Mehul Singla
# Project: Comp-370 Mini Project

SRC_DIR="C:\Users\ABC\Desktop\COMP_370\Comp-370-Mini-Project-\SocketServer\src"
OUT_DIR="out"

echo "Compiling Java source files..."
mkdir -p "$OUT_DIR"
javac -d "$OUT_DIR" $(find "$SRC_DIR" -name "*.java")

if [ $? -ne 0 ]; then
  echo "Compilation failed. Please fix errors above."
  exit 1
fi
echo "Compilation successful."

echo "Starting Heartbeat servers..."
java -cp "$OUT_DIR" "C:\Users\ABC\Desktop\COMP_370\Comp-370-Mini-Project-\Heartbeat Servers and Monitor\src\HeartBeatServer_1.java" &
SERVER1_PID=$!
java -cp "$OUT_DIR" "C:\Users\ABC\Desktop\COMP_370\Comp-370-Mini-Project-\Heartbeat Servers and Monitor\src\HeartBeatServer_2.java" &
SERVER2_PID=$!
java -cp "$OUT_DIR" "C:\Users\ABC\Desktop\COMP_370\Comp-370-Mini-Project-\Heartbeat Servers and Monitor\src\HeartBeatServer_3.java" &
SERVER3_PID=$!


trap "kill $SERVER1_PID $SERVER2_PID $SERVER3_PID" EXIT

sleep 3
echo "Servers started (PIDs: $SERVER1_PID, $SERVER2_PID, $SERVER3_PID)"

echo "Starting Heartbeat Monitor..."
java -cp "$OUT_DIR" "C:\Users\ABC\Desktop\COMP_370\Comp-370-Mini-Project-\Heartbeat Servers and Monitor\src\HeartBeatMonitor.java"

