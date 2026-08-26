#!/usr/bin/env bash
# Cloud Agent install step for AxolotlClient (1.8.9 / Ornithe).
#
# Installs the system packages needed to (a) build the mod and (b) run the
# Minecraft 1.8.9 client head-less via software OpenGL, then builds the module
# and warms the Gradle/Loom/Minecraft caches. Safe to run repeatedly.
set -euo pipefail

# --- System packages ------------------------------------------------------
# Head-less rendering + screenshot automation:
#   xvfb            virtual X11 display
#   xdotool         synthesise mouse/keyboard input
#   ffmpeg          grab frames from the virtual display
#   mesa (dri/glx/egl/gles) + llvmpipe  software OpenGL (no GPU on the VM)
#   libopenal1      Minecraft sound backend (runs in "No Sound" mode head-less)
# The Ornithe/legacy-lwjgl3 runtime bundles SDL3 and loads libEGL/libGL at
# runtime, so the Mesa EGL packages below are what make the GL context work.
PACKAGES=(
  xvfb
  xdotool
  ffmpeg
  libgl1-mesa-dri
  libglx-mesa0
  libegl1
  libegl-mesa0
  libgles2
  libopenal1
)

if command -v sudo >/dev/null 2>&1; then
  sudo apt-get update
  sudo DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends "${PACKAGES[@]}"
else
  apt-get update
  DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends "${PACKAGES[@]}"
fi

# --- Build the 1.8.9 (Ornithe) module + warm caches -----------------------
./gradlew build --no-daemon --stacktrace
