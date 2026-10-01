#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"
jar_path="${DANOGL_JAR:-$repo_root/lib/DanoGameLab.jar}"

if [[ ! -f "$jar_path" ]]; then
    printf '%s\n' 'Missing DanoGameLab.jar. Add it to lib/ or set DANOGL_JAR to its absolute path.' >&2
    exit 1
fi

if ! command -v javac >/dev/null 2>&1; then
    printf '%s\n' 'javac was not found. Install JDK 17 and add its bin directory to PATH.' >&2
    exit 1
fi

mkdir -p build
find src -type f -name '*.java' -print | sort > build/sources.txt
if [[ ! -s build/sources.txt ]]; then
    printf '%s\n' 'No Java source files were found under src/.' >&2
    exit 1
fi

rm -rf -- "$repo_root/build/classes"
mkdir -p build/classes
javac --release 17 -encoding UTF-8 -cp "$jar_path" -d build/classes @build/sources.txt
printf '%s\n' 'Build successful: build/classes'
