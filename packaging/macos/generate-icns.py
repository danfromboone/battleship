#!/usr/bin/env python3
"""
Generates a multi-resolution Apple .icns icon from the SVG asset.
"""
import os
import subprocess
from PIL import Image

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.abspath(os.path.join(SCRIPT_DIR, "../.."))
SVG_PATH = os.path.join(PROJECT_ROOT, "packaging/debian/fleetstrike.svg")
ICNS_PATH = os.path.join(SCRIPT_DIR, "fleetstrike.icns")
PNG_1024 = os.path.join(SCRIPT_DIR, "fleetstrike_1024.png")

print(f"Rendering {SVG_PATH} to 1024x1024 PNG...")
subprocess.run([
    "convert", "-background", "none", "-size", "1024x1024",
    SVG_PATH, PNG_1024
], check=True)

print("Packaging ICNS with resolutions 16, 32, 64, 128, 256, 512, 1024...")
img = Image.open(PNG_1024)
img.save(ICNS_PATH, format="ICNS", sizes=[
    (16, 16), (32, 32), (64, 64), (128, 128),
    (256, 256), (512, 512), (1024, 1024)
])

if os.path.exists(PNG_1024):
    os.remove(PNG_1024)

print(f"Successfully created: {ICNS_PATH}")
