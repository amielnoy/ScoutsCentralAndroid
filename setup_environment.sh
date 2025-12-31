#!/bin/bash

# This script provides instructions for setting up your environment.
# DO NOT run this script directly if you want to update your current shell session.
# Instead, copy and paste the relevant commands for your system.

echo "--- Environment Setup Instructions ---"

# --- macOS / Linux (zsh) ---
# 1. Open your terminal.
# 2. Run these commands:
#    echo 'export ANDROID_HOME=$HOME/Library/Android/sdk' >> ~/.zshrc
#    echo 'export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools' >> ~/.zshrc
# 3. Reload your config:
#    source ~/.zshrc

# --- Windows (PowerShell) ---
# Run these commands in a PowerShell window:
# [Environment]::SetEnvironmentVariable("ANDROID_HOME", "$HOME\AppData\Local\Android\Sdk", "User")
# [Environment]::SetEnvironmentVariable("Path", "[Environment]::GetEnvironmentVariable('Path', 'User');$HOME\AppData\Local\Android\Sdk\platform-tools;$HOME\AppData\Local\Android\Sdk\tools", "User")

echo ""
echo "Please follow the instructions inside this script for your specific OS."
