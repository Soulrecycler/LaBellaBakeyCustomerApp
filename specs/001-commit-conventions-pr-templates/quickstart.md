# Quickstart: Conventional Commit Enforcement & GitHub PR Template

## For contributors (one-time, per clone)

```sh
./gradlew installGitHooks
```

This points Git at the repo's committed `.githooks/` directory (`core.hooksPath`), so the
`commit-msg` hook runs on every local commit from then on.

## What you'll see

- **Good commit** (`feat(cart): add quantity stepper`) → commits normally, no friction.
- **Bad commit** (`fixed the bug`) → rejected immediately with the expected format and an
  example. Fix the message and re-run `git commit`.
- **Merge/revert commits** → never blocked (FR-003).
- **Opening a PR** → the description box is pre-filled with the standard template (Summary /
  Motivation / Testing / Related spec-issue).
- **Any commit in the PR that doesn't conform** → the `commit-lint` required check fails on
  the PR, blocking merge until fixed — this catches it even if a contributor's local hook
  wasn't installed or was bypassed (FR-009).

## Verifying the CI check without waiting for a real PR

There's no local unit-test harness for GitHub Actions in this repo (no `act` or similar
installed). Verify by opening a real PR with one intentionally bad commit and confirming the
`commit-lint` check fails and blocks merge, then fix the commit message and confirm it turns
green.
