#!/bin/bash
# run.sh - Start monitor, 3 server instances, and client
# Usage: ./run.sh

# Get the absolute path to the project root
PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SRC_DIR="$PROJECT_ROOT/src"

echo "========================================="
echo "Starting Heartbeat Monitor System"
echo "========================================="

# Compile all Java files
echo "Compiling Java files..."
javac "$SRC_DIR"/*.java

if [ $? -ne 0 ]; then
    echo "Compilation failed. Please fix errors and try again."
    exit 1
fi

echo "Compilation successful."
echo ""

# Clean up any delay flags from previous runs
rm -f /tmp/heartbeat_delay.flag

# Open Terminal and bring it to the front
osascript -e 'tell application "Terminal" to activate'

# Wait a moment for Terminal to activate
sleep 0.5

# Start Monitor in a new Terminal window
echo "Starting Monitor on port 9000..."
osascript -e 'tell application "Terminal" to do script "cd '"$PROJECT_ROOT"' && echo \"=== MONITOR ===\" && java -cp src Monitor"'
sleep 1

# Start Server 1 (Primary - port 8090) in separate process
echo "Starting Server 1 (port 8090)..."
osascript -e 'tell application "Terminal" to do script "cd '"$PROJECT_ROOT"' && echo \"=== SERVER 1 (8090) ===\" && java -cp src primary 8090"'
sleep 1

# Start Server 2 (Backup - port 8089) in separate process
echo "Starting Server 2 (port 8089)..."
osascript -e 'tell application "Terminal" to do script "cd '"$PROJECT_ROOT"' && echo \"=== SERVER 2 (8089) ===\" && java -cp src backup 8089"'
sleep 1

# Start Server 3 (Backup - port 8088) in separate process
echo "Starting Server 3 (port 8088)..."
osascript -e 'tell application "Terminal" to do script "cd '"$PROJECT_ROOT"' && echo \"=== SERVER 3 (8088) ===\" && java -cp src backup2 8088"'
sleep 1

# Start Client
echo "Starting Client..."
osascript -e 'tell application "Terminal" to do script "cd '"$PROJECT_ROOT"' && echo \"=== CLIENT ===\" && java -cp src Client"'

echo ""
echo "========================================="
echo "All components started successfully!"
echo "========================================="
echo "Monitor:  Port 9000 (heartbeat), 9001 (client API)"
echo "Server 1: Port 8090"
echo "Server 2: Port 8089"
echo "Server 3: Port 8088"
echo ""
echo "Use kill-primary.sh to kill the primary server"
echo "Use kill-backup.sh to kill a backup server"
echo "Use delay-heartbeat.sh to simulate network delays"
echo "========================================="
