#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
APP_DIR="$DIR/Resources/app"
JAR_PATH="$APP_DIR/fleetstrike-1.0.0.jar"

# Attempt to locate Java 21+ on macOS
JAVA_EXEC=""

# 1. Environment variable JAVA_HOME
if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    JAVA_EXEC="$JAVA_HOME/bin/java"
fi

# 2. Apple's java_home utility
if [ -z "$JAVA_EXEC" ] && [ -x "/usr/libexec/java_home" ]; then
    JH=$(/usr/libexec/java_home -v 21+ 2>/dev/null || /usr/libexec/java_home 2>/dev/null || true)
    if [ -n "$JH" ] && [ -x "$JH/bin/java" ]; then
        JAVA_EXEC="$JH/bin/java"
    fi
fi

# 3. Common Homebrew OpenJDK paths (Apple Silicon & Intel)
if [ -z "$JAVA_EXEC" ]; then
    for brew_path in \
        "/opt/homebrew/opt/openjdk@21/bin/java" \
        "/opt/homebrew/opt/openjdk/bin/java" \
        "/usr/local/opt/openjdk@21/bin/java" \
        "/usr/local/opt/openjdk/bin/java"; do
        if [ -x "$brew_path" ]; then
            JAVA_EXEC="$brew_path"
            break
        fi
    done
fi

# 4. Fallback to system PATH
if [ -z "$JAVA_EXEC" ] && command -v java >/dev/null 2>&1; then
    JAVA_EXEC="java"
fi

# If no Java found, display native macOS alert
if [ -z "$JAVA_EXEC" ]; then
    if command -v osascript >/dev/null 2>&1; then
        osascript -e 'display alert "Java Required" message "Fleet Strike requires Java 21 or newer to run. Please install Java from https://adoptium.net or via Homebrew (brew install openjdk@21)." as critical'
    fi
    echo "ERROR: Java 21+ is required to run Fleet Strike. Please install a compatible JRE/JDK." >&2
    exit 1
fi

# Execute application with macOS native integration flags
exec "$JAVA_EXEC" \
    -Xdock:name="Fleet Strike" \
    -Xdock:icon="$DIR/Resources/fleetstrike.icns" \
    -Dapple.laf.useScreenMenuBar=true \
    -Dcom.apple.mrj.application.apple.menu.about.name="Fleet Strike" \
    -jar "$JAR_PATH" "$@"
