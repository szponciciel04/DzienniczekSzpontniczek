#!/usr/bin/env sh
set -eu

project_dir=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
install_prefix=${DZIENNICZEK_PREFIX:-"$HOME/.local"}

while [ "$#" -gt 0 ]; do
  case "$1" in
    --prefix) install_prefix=$2; shift 2 ;;
    *) echo "Unknown option: $1" >&2; exit 2 ;;
  esac
done

if ! command -v java >/dev/null 2>&1; then
  echo "Java 17+ is required. macOS: brew install openjdk@17; Linux/WSL: install openjdk-17-jre." >&2
  exit 1
fi

java_major=$(java -version 2>&1 | awk -F '[\".]' '/version/ { print ($2 == 1 ? $3 : $2); exit }')
if [ "${java_major:-0}" -lt 17 ]; then
  echo "Java 17+ is required; found Java ${java_major:-unknown}." >&2
  exit 1
fi

cd "$project_dir"
./gradlew :cli:installDist --no-daemon

version=0.2.0
target="$install_prefix/lib/dzienniczek-$version"
mkdir -p "$install_prefix/lib" "$install_prefix/bin"
if [ -e "$target" ]; then
  backup="$target.previous"
  if [ -e "$backup" ]; then
    echo "Refusing to overwrite existing backup: $backup" >&2
    exit 1
  fi
  mv "$target" "$backup"
fi
cp -R "$project_dir/cli/build/install/dzienniczek" "$target"
ln -sfn "$target/bin/dzienniczek" "$install_prefix/bin/dzienniczek"

echo "Installed $install_prefix/bin/dzienniczek"
