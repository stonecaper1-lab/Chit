#!/usr/bin/env bash
# Usage: bash release.sh <version> [commit message]
# Commits, pushes, waits for CI, tags v<version>, waits for the release build,
# then downloads the signed APK to your Downloads folder.
set -euo pipefail
cd "$(dirname "$0")"

VER="${1:?usage: bash release.sh <version, e.g. 0.9.1> [commit message]}"
MSG="${2:-Chit $VER}"
TAG="v$VER"

grep -q "versionName = \"$VER\"" app/build.gradle.kts \
  || { echo "app/build.gradle.kts versionName is not $VER"; exit 1; }

git add -A
git commit -m "$MSG" || echo "Nothing new to commit"
git push origin main
SHA=$(git rev-parse HEAD)

wait_run() {  # $1 = workflow file
  local run=""
  for _ in $(seq 1 12); do
    run=$(gh run list --workflow "$1" --commit "$SHA" --limit 1 --json databaseId -q '.[0].databaseId' || true)
    [ -n "$run" ] && break
    sleep 5
  done
  [ -n "$run" ] || { echo "No $1 run found for $SHA"; exit 1; }
  gh run watch "$run" --exit-status
}

echo "== Waiting for CI =="
wait_run ci.yml

echo "== CI green. Tagging $TAG =="
git tag "$TAG"
git push origin "$TAG"

echo "== Waiting for release build =="
wait_run release.yml

DEST="$HOME/storage/downloads"
[ -d "$DEST" ] || DEST="$HOME"
gh release download "$TAG" -p "*.apk" -D "$DEST" --clobber
echo "== Done. APK in $DEST =="
ls -l "$DEST"/Chit-"$TAG".apk
