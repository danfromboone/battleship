#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

BUILD_DIR="$SCRIPT_DIR/build"

# Ensure jar is built
if [ ! -f "$BUILD_DIR/libs/battleship-1.0.0.jar" ]; then
    echo "=== Building Application JAR ==="
    ./gradlew jar
fi

VERSION=$(grep "version = " build.gradle | head -n1 | cut -d"'" -f2)
APP_NAME="Battleship"
BUNDLE_DIR="$BUILD_DIR/macos/${APP_NAME}.app"
DIST_DIR="$BUILD_DIR/distributions"

echo "=== Staging macOS Application Bundle (${APP_NAME}.app) ==="
rm -rf "$BUNDLE_DIR"
mkdir -p "$DIST_DIR"

# 1. Directory Structure
mkdir -p "$BUNDLE_DIR/Contents/MacOS"
mkdir -p "$BUNDLE_DIR/Contents/Resources/app"

# 2. Info.plist & PkgInfo
cp packaging/macos/Info.plist "$BUNDLE_DIR/Contents/Info.plist"
cp packaging/macos/PkgInfo "$BUNDLE_DIR/Contents/PkgInfo"

# Update version in Info.plist if needed
sed -i "s|<string>1.0.0</string>|<string>${VERSION}</string>|g" "$BUNDLE_DIR/Contents/Info.plist"

# 3. Executable launcher
cp packaging/macos/launcher.sh "$BUNDLE_DIR/Contents/MacOS/battleship"
chmod 755 "$BUNDLE_DIR/Contents/MacOS/battleship"

# 4. Icon
cp packaging/macos/battleship.icns "$BUNDLE_DIR/Contents/Resources/battleship.icns"

# 5. Application JAR & Docs
cp "$BUILD_DIR/libs/battleship-${VERSION}.jar" "$BUNDLE_DIR/Contents/Resources/app/"
cp README.md "$BUNDLE_DIR/Contents/Resources/"

echo "=== Packaging macOS Distribution Archives ==="
ZIP_OUTPUT="$DIST_DIR/${APP_NAME}-${VERSION}-macOS.zip"
TAR_OUTPUT="$DIST_DIR/${APP_NAME}-${VERSION}-macOS.tar.gz"

rm -f "$ZIP_OUTPUT" "$TAR_OUTPUT"

# Create zip preserving executable permissions (-y preserves symlinks)
(cd "$BUILD_DIR/macos" && zip -q -r -y "$ZIP_OUTPUT" "${APP_NAME}.app")

# Create tar.gz
(cd "$BUILD_DIR/macos" && tar -czf "$TAR_OUTPUT" "${APP_NAME}.app")

echo ""
echo ">>> SUCCESS: macOS application packaged at:"
echo "    $BUNDLE_DIR"
echo "    $ZIP_OUTPUT"
echo "    $TAR_OUTPUT"
echo ""
echo "To run on macOS:"
echo "    1. Unzip ${APP_NAME}-${VERSION}-macOS.zip (or move ${APP_NAME}.app to /Applications)"
echo "    2. Double-click ${APP_NAME}.app to launch!"
echo "       (Or run from Terminal: open ${APP_NAME}.app)"
