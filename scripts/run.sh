#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"
jar_path="${DANOGL_JAR:-$repo_root/lib/DanoGameLab.jar}"

bash "$repo_root/scripts/build.sh"
exec java -cp "$repo_root/build/classes:$jar_path" pepse.PepseGameManager
