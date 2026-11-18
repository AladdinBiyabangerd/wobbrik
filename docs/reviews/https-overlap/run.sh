#!/bin/sh
# Opt-in focused host measurement; not CI. Uses an isolated local server and fake token.
set -eu
cd "$(dirname "$0")/../../.."
fixture_dir=/private/tmp/wobbrik-https
mkdir -p "$fixture_dir"
# This fixture has a fixed temporary directory: do not run concurrent copies.
openssl req -x509 -newkey rsa:2048 -nodes \
  -keyout "$fixture_dir/key.pem" -out "$fixture_dir/cert.pem" -days 2 \
  -subj '/CN=localhost' -addext 'subjectAltName=DNS:localhost,IP:127.0.0.1' \
  > "$fixture_dir/cert-generation.log" 2>&1
rm -f "$fixture_dir/origin"
python3 docs/reviews/https-overlap/https_fixture.py > "$fixture_dir/server.log" 2>&1 &
fixture_pid=$!
trap 'kill "$fixture_pid" 2>/dev/null || true; wait "$fixture_pid" 2>/dev/null || true' EXIT INT TERM
attempt=0
while [ ! -f "$fixture_dir/origin" ]; do
  kill -0 "$fixture_pid"
  attempt=$((attempt + 1))
  [ "$attempt" -lt 100 ]
  sleep 0.05
done
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' \
  android-billing/gradlew -p android-billing :app:testDebugUnitTest \
  --tests '*HttpsOverlapTest' \
  -I "$PWD/docs/reviews/https-overlap/measure.init.gradle" --offline --console=plain
