#!/bin/bash
export DISPLAY=:1
export LIBGL_ALWAYS_SOFTWARE=1
export GALLIUM_DRIVER=llvmpipe

echo "=== Preparing Mini-Games World ==="

# prepare world folder
mkdir -p run/saves
if [ -d "template-world" ]; then
  cp -r template-world run/saves/DemoWorld
fi

# optimization
mkdir -p run
cat << 'EOF' > run/options.txt
graphicsMode:0
renderDistance:6
simulationDistance:4
fullscreen:false
guiScale:3
fov:0.0
soundCategory_master:0.8
EOF

# 20 min auto timeout
(
  sleep 1200
  echo "Time limit reached(20 minutes). Shutting down demo session..."
  killall -u $(whoami) java 2>/dev/null
) &

# launch mc directly
echo "=== Launching Minecraft Demo ==="
./gradlew runClient --args="--quickPlaySingleplayer DemoWorld"