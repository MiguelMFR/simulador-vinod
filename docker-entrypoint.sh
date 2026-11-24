#!/bin/sh
set -e

rm -rf /tmp/.X99-lock /tmp/.X11-unix/X99

SCREEN_WIDTH="${SCREEN_WIDTH:-1600}"
SCREEN_HEIGHT="${SCREEN_HEIGHT:-900}"

Xvfb :99 -screen 0 "${SCREEN_WIDTH}x${SCREEN_HEIGHT}x24" -nolisten tcp &
XVFB_PID=$!

sleep 1
fluxbox >/tmp/fluxbox.log 2>&1 &
X11VNC_PID=""

x11vnc -display :99 -forever -shared -nopw -listen 0.0.0.0 -rfbport 5900 >/tmp/x11vnc.log 2>&1 &
X11VNC_PID=$!

websockify --web /usr/share/novnc 6080 0.0.0.0:5900 >/tmp/websockify.log 2>&1 &
WEBSOCKIFY_PID=$!

cleanup() {
    kill "$WEBSOCKIFY_PID" "$X11VNC_PID" "$XVFB_PID" 2>/dev/null || true
}
trap cleanup EXIT INT TERM

sleep 1
exec "$@"
