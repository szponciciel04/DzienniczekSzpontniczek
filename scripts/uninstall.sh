#!/usr/bin/env sh
set -eu

install_prefix=${DZIENNICZEK_PREFIX:-"$HOME/.local"}
target="$install_prefix/lib/dzienniczek-0.2.0"
link="$install_prefix/bin/dzienniczek"

if [ -f "$link" ] && grep -q "Dzienniczek CLI local launcher" "$link"; then
  mv "$link" "$link.uninstalled"
fi
if [ -d "$target" ]; then
  mv "$target" "$target.uninstalled"
  echo "Moved installation to $target.uninstalled (recoverable)."
fi
