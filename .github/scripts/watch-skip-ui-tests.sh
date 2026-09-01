#!/usr/bin/env bash
# Polls the pull request for the SkipUiTests label and cancels this workflow run
# when it appears after the job has already started.
set -euo pipefail

if [[ -z "${GH_TOKEN:-${GITHUB_TOKEN:-}}" ]]; then
  echo "No GitHub token; SkipUiTests watchdog disabled" >&2
  exit 0
fi

PR_NUMBER="${1:-${PR_NUMBER:-}}"
if [[ -z "$PR_NUMBER" ]]; then
  echo "Usage: watch-skip-ui-tests.sh <pr-number>" >&2
  exit 1
fi

export GH_TOKEN="${GH_TOKEN:-$GITHUB_TOKEN}"

echo "Watching PR #${PR_NUMBER} for SkipUiTests"

while true; do
  sleep 15
  if gh pr view "$PR_NUMBER" --repo "$GITHUB_REPOSITORY" --json labels --jq '.labels[].name' \
    | grep -Fxq 'SkipUiTests'; then
    echo "SkipUiTests label detected on PR #${PR_NUMBER}; cancelling run ${GITHUB_RUN_ID}"
    gh run cancel "$GITHUB_RUN_ID" --repo "$GITHUB_REPOSITORY"
    exit 0
  fi
done
