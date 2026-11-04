#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

BUILD_DIR="$SCRIPT_DIR/build"

# Ensure JAR is built
if [ ! -f "$BUILD_DIR/libs/fleetstrike-1.0.0.jar" ]; then
    echo "=== Building Application JAR ==="
    ./gradlew jar
fi

VERSION=$(grep "version = " build.gradle | head -n1 | cut -d"'" -f2)
APP_NAME="FleetStrike"
STAGE_DIR="$BUILD_DIR/windows/${APP_NAME}"
DIST_DIR="$BUILD_DIR/distributions"

echo "=== Staging Windows Portable Distribution (${APP_NAME}) ==="
rm -rf "$STAGE_DIR"
mkdir -p "$STAGE_DIR/lib"
mkdir -p "$STAGE_DIR/icon"
mkdir -p "$DIST_DIR"

# 1. Copy Application JAR
cp "$BUILD_DIR/libs/fleetstrike-${VERSION}.jar" "$STAGE_DIR/lib/"

# 2. Copy Launchers
cp packaging/windows/FleetStrike.bat "$STAGE_DIR/"
cp packaging/windows/FleetStrike.vbs "$STAGE_DIR/"
cp packaging/windows/FleetStrike-Console.bat "$STAGE_DIR/"
cp packaging/windows/CreateShortcut.vbs "$STAGE_DIR/"

# 3. Copy Icon
cp packaging/windows/fleetstrike.ico "$STAGE_DIR/icon/"

# 4. Copy Documentation with Windows CRLF line endings
sed 's/$/\r/' README.md > "$STAGE_DIR/README.txt"

# 5. Create Zip Archive
ZIP_OUTPUT="$DIST_DIR/${APP_NAME}-${VERSION}-Windows.zip"
rm -f "$ZIP_OUTPUT"

echo "=== Packaging Windows Distribution Archive ==="
(cd "$BUILD_DIR/windows" && zip -q -r "$ZIP_OUTPUT" "${APP_NAME}")

echo ""
echo ">>> SUCCESS: Windows distribution packaged at:"
echo "    $STAGE_DIR"
echo "    $ZIP_OUTPUT"
echo ""
echo "To run on Windows:"
echo "    1. Unzip ${APP_NAME}-${VERSION}-Windows.zip"
echo "    2. Double-click 'FleetStrike.vbs' or 'FleetStrike.bat' to launch the GUI!"
echo "    3. (Optional) Run 'CreateShortcut.vbs' to place a shortcut on your Desktop."
echo "    4. Or run 'FleetStrike-Console.bat' for terminal mode."
