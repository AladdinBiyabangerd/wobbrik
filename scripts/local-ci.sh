#!/bin/sh
set -eu
repo_dir=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
cd "$repo_dir"
current_branch=$(git symbolic-ref --quiet --short HEAD || true)
if [ "$current_branch" != "main" ]; then
    echo "Local CI runs only on main after merging a reviewed branch (current: ${current_branch:-detached HEAD})." >&2
    exit 1
fi
git diff --check
git diff --cached --check
if [ -z "${JAVA_HOME:-}" ] && [ -d "/Applications/Android Studio.app/Contents/jbr/Contents/Home" ]; then
    JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
    export JAVA_HOME
fi
if [ -z "${ANDROID_HOME:-}" ] && [ -d "$HOME/Library/Android/sdk" ]; then
    ANDROID_HOME="$HOME/Library/Android/sdk"
    export ANDROID_HOME
fi
cd android-billing
./gradlew ci --no-daemon --console=plain "$@"
