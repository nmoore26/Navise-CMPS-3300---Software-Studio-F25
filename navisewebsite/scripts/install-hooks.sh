set -e
HOOK_SRC=".githooks"
HOOK_DST=".git/hooks"

if [ ! -d "$HOOK_SRC" ]; then
  echo "No $HOOK_SRC directory found. Make sure .githooks exists in the repo."
  exit 1
fi

mkdir -p "$HOOK_DST"
cp -r "$HOOK_SRC"/* "$HOOK_DST"/
chmod +x "$HOOK_DST"/*
echo "Hooks installed to $HOOK_DST. Run this once after cloning: bash scripts/install-hooks.sh"
