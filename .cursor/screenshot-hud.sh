#!/usr/bin/env bash
# Launch Minecraft 1.8.9 (Ornithe / DolphinClient) head-less, create a fresh
# Creative super-flat singleplayer world, press Right Shift to open the
# DolphinClient HUD editor, and save a screenshot.
#
# Usage:  .cursor/screenshot-hud.sh [output.png]
# Default output: /workspace/dolphinClient-hud.png
#
# Requires the packages installed by .cursor/install.sh (xvfb, xdotool, ffmpeg,
# mesa EGL/GL). All rendering is software (llvmpipe) since the VM has no GPU.
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

OUT="${1:-$REPO_ROOT/dolphinclient-hud.png}"
DISPLAY_NUM="${DISPLAY_NUM:-99}"
SCREEN_W=1280
SCREEN_H=720
LOG="$(mktemp /tmp/dolphin-runclient.XXXXXX.log)"

export DISPLAY=":$DISPLAY_NUM"
# Software OpenGL. legacy-lwjgl3 requests a GL 3.2 *compatibility* context via
# SDL3; force EGL (Xvfb's GLX can't supply that FBConfig) and raise Mesa's
# compat-profile ceiling so llvmpipe will create it.
export LIBGL_ALWAYS_SOFTWARE=1
export GALLIUM_DRIVER=llvmpipe
export MESA_GL_VERSION_OVERRIDE=4.6COMPAT
export MESA_GLSL_VERSION_OVERRIDE=460
export SDL_VIDEODRIVER=x11
export SDL_VIDEO_FORCE_EGL=1
export LEGACY_LWJGL3_USE_SDL=true

# Optional: set RECORD=1 to also capture a video of the interactive flow.
RECORD="${RECORD:-0}"
VIDEO="${VIDEO:-${OUT%.png}.mp4}"

XVFB_PID=""
GAME_PGID=""
FFMPEG_PID=""
cleanup() {
  [ -n "$FFMPEG_PID" ] && kill -INT "$FFMPEG_PID" 2>/dev/null || true
  [ -n "$GAME_PGID" ] && kill -TERM -"$GAME_PGID" 2>/dev/null || true
  [ -n "$XVFB_PID" ] && kill -TERM "$XVFB_PID" 2>/dev/null || true
}
trap cleanup EXIT

grab() { ffmpeg -y -f x11grab -video_size "${SCREEN_W}x${SCREEN_H}" -i ":$DISPLAY_NUM" -frames:v 1 -update 1 "$1" 2>/dev/null; }
click() { xdotool mousemove "$1" "$2" click 1; sleep "${3:-1}"; }

# --- Virtual display ------------------------------------------------------
Xvfb ":$DISPLAY_NUM" -screen 0 "${SCREEN_W}x${SCREEN_H}x24" -ac +extension GLX +render -noreset >/tmp/dolphin-xvfb.log 2>&1 &
XVFB_PID=$!
sleep 3

# --- Start from a clean world list for deterministic menu navigation ------
rm -rf "$REPO_ROOT/versions/1.8.9/run/saves" 2>/dev/null || true

# --- Launch the client (own process group so we can stop it cleanly) ------
setsid bash -c 'exec ./gradlew :1.8.9:runClient --no-daemon --console=plain' >"$LOG" 2>&1 &
GAME_PGID=$!

# Wait until the main menu is up (sound engine init is a late, reliable marker).
echo "Waiting for the client to reach the title screen..."
for _ in $(seq 1 90); do
  grep -q "Sound engine started" "$LOG" && break
  sleep 2
done
sleep 5

if [ "$RECORD" = "1" ]; then
  ffmpeg -y -f x11grab -video_size "${SCREEN_W}x${SCREEN_H}" -framerate 15 -i ":$DISPLAY_NUM" \
    -pix_fmt yuv420p "$VIDEO" >/tmp/dolphin-ffmpeg.log 2>&1 &
  FFMPEG_PID=$!
fi

# The client window is 854x480, centred in the 1280x720 display. The clicks
# below drive the vanilla menus at that fixed geometry.
click 637 330 2   # Singleplayer
click 795 514 2   # Create New World
click 637 367 1   # Game Mode -> Hardcore
click 637 367 1   # Game Mode -> Creative
click 637 512 1   # More World Options...
click 796 339 1   # World Type -> Superflat
click 477 563 1   # Create New World (generate + load)

echo "Waiting for the world to load..."
sleep 18

xdotool key Shift_R   # open the DolphinClient HUD editor
sleep 3

grab "$OUT"
echo "Saved screenshot to: $OUT"

if [ "$RECORD" = "1" ] && [ -n "$FFMPEG_PID" ]; then
  sleep 2
  kill -INT "$FFMPEG_PID" 2>/dev/null || true
  wait "$FFMPEG_PID" 2>/dev/null || true
  FFMPEG_PID=""
  echo "Saved video to: $VIDEO"
fi
