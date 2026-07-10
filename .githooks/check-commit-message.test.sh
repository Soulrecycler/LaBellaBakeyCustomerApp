#!/bin/sh
# Self-check for check-commit-message.sh. Run: sh .githooks/check-commit-message.test.sh
# Exits 0 if all cases pass, 1 (with the first failure) otherwise.

script_dir=$(dirname "$0")
checker="$script_dir/check-commit-message.sh"
failures=0

# expect_pass "<header>" — checker should exit 0
expect_pass() {
  if "$checker" "$1" >/dev/null 2>&1; then
    echo "ok   (accepted) $1"
  else
    echo "FAIL (should accept) $1"
    failures=$((failures + 1))
  fi
}

# expect_fail "<header>" — checker should exit non-zero
expect_fail() {
  if "$checker" "$1" >/dev/null 2>&1; then
    echo "FAIL (should reject) $1"
    failures=$((failures + 1))
  else
    echo "ok   (rejected) $1"
  fi
}

# Valid conventional commits (FR-002)
expect_pass "feat(cart): add quantity stepper"
expect_pass "fix: correct cart total rounding"
expect_pass "chore(ci): bump action version"
expect_pass "docs: update readme"

# Git-generated / workflow messages that must NOT be blocked (FR-003)
expect_pass "Merge branch 'main' into feature"
expect_pass "Revert \"feat: something\""
expect_pass "fixup! feat(cart): add quantity stepper"
expect_pass "squash! fix: rounding"

# Invalid messages that must be rejected (FR-001/FR-004)
expect_fail "fixed the bug"
expect_fail "WIP"
expect_fail "feat add stepper"
expect_fail "Feat: capitalized type"
expect_fail "feat(cart):missing space"

if [ "$failures" -eq 0 ]; then
  echo "ALL PASS"
  exit 0
fi
echo "$failures FAILURE(S)"
exit 1
