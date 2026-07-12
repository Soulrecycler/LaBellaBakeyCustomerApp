#!/bin/sh
# Single source of truth for the Conventional Commits check.
# Called by both the local commit-msg hook and the commit-lint CI workflow.
#
# Usage: check-commit-message.sh "<commit message header (first line)>"
# Exit:  0 if the header is valid or is a Git-generated message that must not be blocked;
#        1 with FR-004 guidance on stderr otherwise.

header="$1"

# Don't block Git-generated / Git-workflow messages a contributor didn't author (FR-003).
case "$header" in
  "Merge "*|"Revert "*|"fixup!"*|"squash!"*) exit 0 ;;
esac

# Description (after ": ") must contain at least one non-space char — a
# whitespace-only description is not a real message.
pattern='^(feat|fix|docs|style|refactor|perf|test|build|ci|chore|revert)(\([a-zA-Z0-9_.-]+\))?: .*[^[:space:]]'

if echo "$header" | grep -qE "$pattern"; then
  exit 0
fi

cat >&2 <<EOF
❌ Commit message does not follow Conventional Commits format.

   Expected: <type>(<scope>): <description>
   Types:    feat, fix, docs, style, refactor, perf, test, build, ci, chore, revert
   Example:  feat(cart): add quantity stepper

   Your message header was:
   "$header"
EOF
exit 1
