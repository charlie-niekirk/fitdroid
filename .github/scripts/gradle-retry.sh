#!/usr/bin/env bash
# Retry Gradle when configuration fails on transient repository errors.
# Real test/lint failures are not retried.
set -uo pipefail

max="${GRADLE_RETRY_ATTEMPTS:-4}"
attempt=1
delay=15

while true; do
  log="$(mktemp)"
  set +e
  ./gradlew "$@" 2>&1 | tee "$log"
  status=${PIPESTATUS[0]}
  set -e
  if [[ "$status" -eq 0 ]]; then
    rm -f "$log"
    exit 0
  fi

  if grep -Eqi 'could not resolve|was not found in any of the following sources|Connection reset|Timeout|Gateway Time-out|HTTP [45]0[0-9]|Unknown host|Network is unreachable' "$log"; then
    if (( attempt >= max )); then
      rm -f "$log"
      exit "$status"
    fi
    echo "Transient Gradle resolution failure (attempt ${attempt}/${max}); retrying in ${delay}s..."
    rm -f "$log"
    sleep "$delay"
    attempt=$((attempt + 1))
    delay=$((delay * 2))
    continue
  fi

  rm -f "$log"
  exit "$status"
done
