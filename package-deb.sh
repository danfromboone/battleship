#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

BUILD_DIR="$SCRIPT_DIR/build"

if [ ! -d "$BUILD_DIR/install/battleship" ]; then
    echo "=== Building Battleship Application Distribution ==="
    ./gradlew installDist
fi
VERSION=$(grep "version = " build.gradle | head -n1 | cut -d"'" -f2)
PKG_NAME="battleship_${VERSION}_all"
DEB_ROOT="$BUILD_DIR/debian/$PKG_NAME"
DIST_DIR="$BUILD_DIR/distributions"

echo "=== Staging Debian Package ($PKG_NAME) ==="
rm -rf "$DEB_ROOT"
mkdir -p "$DIST_DIR"

# 1. Install app files into /usr/share/battleship
mkdir -p "$DEB_ROOT/usr/share/battleship"
cp -r "$BUILD_DIR/install/battleship/bin" "$DEB_ROOT/usr/share/battleship/"
cp -r "$BUILD_DIR/install/battleship/lib" "$DEB_ROOT/usr/share/battleship/"
rm -f "$DEB_ROOT/usr/share/battleship/bin/"*.bat
chmod +x "$DEB_ROOT/usr/share/battleship/bin/battleship"

# 2. Symlink to /usr/bin/battleship
mkdir -p "$DEB_ROOT/usr/bin"
ln -sf /usr/share/battleship/bin/battleship "$DEB_ROOT/usr/bin/battleship"

# 3. Desktop Application Entry
mkdir -p "$DEB_ROOT/usr/share/applications"
cp packaging/debian/battleship.desktop "$DEB_ROOT/usr/share/applications/"

# 4. Application Icons
mkdir -p "$DEB_ROOT/usr/share/icons/hicolor/scalable/apps"
mkdir -p "$DEB_ROOT/usr/share/pixmaps"
cp packaging/debian/battleship.svg "$DEB_ROOT/usr/share/icons/hicolor/scalable/apps/"
cp packaging/debian/battleship.svg "$DEB_ROOT/usr/share/pixmaps/"

# 5. Documentation & License
mkdir -p "$DEB_ROOT/usr/share/doc/battleship"
cp packaging/debian/copyright "$DEB_ROOT/usr/share/doc/battleship/"
cp README.md "$DEB_ROOT/usr/share/doc/battleship/"

# 6. Calculate Installed-Size in KB
INSTALLED_SIZE=$(du -sk "$DEB_ROOT" | cut -f1)

# 7. Generate DEBIAN/control
mkdir -p "$DEB_ROOT/DEBIAN"
cat <<EOF > "$DEB_ROOT/DEBIAN/control"
Package: battleship
Version: $VERSION
Section: games
Priority: optional
Architecture: all
Installed-Size: $INSTALLED_SIZE
Maintainer: Dan <danfromboone@gmail.com>
Depends: default-jre (>= 2:1.21) | openjdk-21-jre | openjdk-21-jre-headless | java21-runtime
Homepage: https://github.com/danfromboone/battleship
Description: Classic Battleship tactical naval warfare game
 Battleship is a classic naval strategy game built with Java 21 adhering
 to Domain-Driven Design (DDD) principles. Features both a rich Swing GUI
 presentation layer with interactive radar targeting, fleet placement hover
 previews, pass-and-play hotseat privacy protection, and smart Hunt-and-Target AI,
 as well as a full-featured ANSI console terminal interface.
EOF
chmod 644 "$DEB_ROOT/DEBIAN/control"

echo "=== Building .deb Archive with dpkg-deb ==="
DEB_FILE="$DIST_DIR/${PKG_NAME}.deb"
dpkg-deb --build --root-owner-group "$DEB_ROOT" "$DEB_FILE"

echo ""
echo ">>> SUCCESS: Debian package generated at:"
echo "    $DEB_FILE"
echo ""
echo "To inspect the package:"
echo "    dpkg-deb --info $DEB_FILE"
echo "    dpkg-deb --contents $DEB_FILE"
echo ""
echo "To install on Debian/Ubuntu/Mint:"
echo "    sudo apt install $DEB_FILE"
echo "    # or: sudo dpkg -i $DEB_FILE && sudo apt-get install -f"
