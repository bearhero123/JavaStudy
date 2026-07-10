#!/usr/bin/env bash
# Publish the version currently on GitHub main to this server.
set -Eeuo pipefail

REPO_DIR="${REPO_DIR:-/opt/mis-study/source}"
RUNTIME_DIR="${RUNTIME_DIR:-/opt/mis-study/backend}"
WEB_ROOT="${WEB_ROOT:-/var/www/mis-study}"
WEB_RELEASES="${WEB_RELEASES:-/var/www/mis-study-releases}"
NODE_HOME="${NODE_HOME:-/opt/mis-study/tools/node}"
BRANCH="${BRANCH:-main}"

die() {
  printf 'Release aborted: %s\n' "$*" >&2
  exit 1
}

[[ -d "$REPO_DIR/.git" ]] || die "source checkout not found: $REPO_DIR"
[[ -f "$RUNTIME_DIR/application.yaml" ]] || die "runtime configuration not found: $RUNTIME_DIR/application.yaml"
[[ -x "$NODE_HOME/bin/node" ]] || die "project Node runtime not found: $NODE_HOME/bin/node"
[[ -x "$NODE_HOME/bin/npm" ]] || die "project npm runtime not found: $NODE_HOME/bin/npm"

export PATH="$NODE_HOME/bin:$PATH"

cd "$REPO_DIR"
[[ -z "$(git status --porcelain)" ]] || die "source checkout has uncommitted changes"
git pull --ff-only origin "$BRANCH"
REVISION="$(git rev-parse --short HEAD)"

(
  cd mis-study-ui
  npm ci --no-audit --no-fund
  npm run build
)

(
  cd mis-study-backend
  bash ./mvnw -Dmaven.repo.local=/opt/mis-study/.m2repo clean package -DskipTests
)

JAR="$(find "$REPO_DIR/mis-study-backend/target" -maxdepth 1 -type f -name '*.jar' ! -name '*.jar.original' -print -quit)"
[[ -n "$JAR" ]] || die "backend JAR was not created"

RELEASE_DIR="$WEB_RELEASES/$REVISION"
install -d -m 0755 "$RELEASE_DIR"
cp -a "$REPO_DIR/mis-study-ui/dist/." "$RELEASE_DIR/"

if [[ -L "$WEB_ROOT" ]]; then
  PREVIOUS_WEB_ROOT="$(readlink -f "$WEB_ROOT")"
elif [[ -d "$WEB_ROOT" ]]; then
  PREVIOUS_WEB_ROOT="$WEB_RELEASES/legacy-$(date +%Y%m%d%H%M%S)"
  install -d -m 0755 "$WEB_RELEASES"
  mv "$WEB_ROOT" "$PREVIOUS_WEB_ROOT"
else
  die "web root not found: $WEB_ROOT"
fi

ln -s "$RELEASE_DIR" "$WEB_ROOT.next"
mv -Tf "$WEB_ROOT.next" "$WEB_ROOT"

install -m 0644 "$JAR" "$RUNTIME_DIR/mis-study-backend.jar.new"
cp -p "$RUNTIME_DIR/mis-study-backend.jar" "$RUNTIME_DIR/mis-study-backend.jar.previous"
mv -f "$RUNTIME_DIR/mis-study-backend.jar.new" "$RUNTIME_DIR/mis-study-backend.jar"

if ! systemctl restart mis-study-backend.service || ! systemctl is-active --quiet mis-study-backend.service; then
  cp -p "$RUNTIME_DIR/mis-study-backend.jar.previous" "$RUNTIME_DIR/mis-study-backend.jar"
  ln -s "$PREVIOUS_WEB_ROOT" "$WEB_ROOT.rollback"
  mv -Tf "$WEB_ROOT.rollback" "$WEB_ROOT"
  systemctl restart mis-study-backend.service || true
  die "service restart failed; the previous application files were restored"
fi

printf 'Release %s completed successfully.\n' "$REVISION"
