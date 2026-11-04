#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "=== Packaging Battleship RPM for Red Hat, Fedora, openSUSE, and CentOS ==="
./gradlew packageRpm

RPM_FILE=$(find build/distributions -name "battleship-*.noarch.rpm" | head -n1)

echo ""
echo "To install on Fedora / RHEL / CentOS / Rocky Linux / AlmaLinux:"
echo "    sudo dnf install $RPM_FILE"
echo ""
echo "To install on openSUSE / SLES:"
echo "    sudo zypper install $RPM_FILE"
echo ""
echo "Or using rpm directly:"
echo "    sudo rpm -ivh $RPM_FILE"
echo ""
echo "To query package info after install:"
echo "    rpm -qil battleship"
