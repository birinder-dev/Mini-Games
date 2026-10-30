#!/bin/bash
export DISPLAY=:1
export LIBGL_ALWAYS_SOFTWARE=1
export GALLIUM_DRIVER=llvmpipe

rm -f /usr/local/novnc/ready.txt /tmp/demo_ready

echo "=== Preparing Mini-Games World ==="

mkdir -p run/saves
if [ -d "template-world" ]; then
  cp -r template-world run/saves/DemoWorld
fi

# optimization
mkdir -p run
cat << 'EOF' > run/options.txt
graphicsMode:0
renderDistance:2
simulationDistance:2
ao:0
clouds:0
particles:2
entityShadows:false
mipmapLevels:0
biomeBlendRadius:0
maxFps:60
fullscreen:false
guiScale:3
fov:0.0
soundCategory_master:0.8
EOF

(
echo "Waiting for Minecraft to launch and join world..."
while true; do
  sleep 2
  if xdotool search --onlyvisible --name "Minecraft" > /dev/null 2>&1; then
    touch /usr/local/novnc/ready.txt /tmp/demo_ready
    break
  fi
  done
) &
(
waited=0
connected=false
while [ $waited -lt 600 ]; do
  if ss -tn '( sport = :6080 or sport = :5901 )' 2>/dev/null | grep -q ESTAB; then
    connected=true
    echo "=== Player connected to demo session ==="
    break
  fi
  sleep 5
  waited=$((waited + 5))
done

if [ "$connected" = false ]; then
  echo "None connected. Stopping demo..."
else
  idle=0
  while true; do
    sleep 10
    if ss -tn '( sport = :6080 or sport = :5901 )' 2>/dev/null | grep -q ESTAB; then
      idle=0
    else
      idle=$((idle + 10))
      if [ $idle -ge 60 ]; then
        echo "Player Disconnected. Shutting down session..."
        break
      fi
    fi
  done
fi

pkill -u $(whoami) -f java 2>/dev/null
if [ -n "$CODESPACE_NAME" ]; then
  gh codespace stop -c "$CODESPACE_NAME" 2>/dev/null
fi
) &

echo "=== Launching Minecraft Demo ==="
./gradlew runClient -Dorg.gradle.jvmargs="-Xms1G -Xmx2G" --args="--quickPlaySingleplayer DemoWorld --width 1024 --height 576"