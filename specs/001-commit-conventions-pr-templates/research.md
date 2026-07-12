# Phase 0 Research: Conventional Commit Enforcement & GitHub PR Template

## Decision 1: Share one validation implementation between the local hook and CI

**Decision**: Extract the Conventional Commits regex/type-list check out of `.githooks/commit-msg`
into a small standalone script, `.githooks/check-commit-message.sh`, that takes a single commit
message header string and exits 0/1 with the same error text. Both the local git hook and the
CI workflow call this one script.

**Rationale**: FR-002 defines one canonical set of accepted types and format. If the regex were
duplicated (once in the shell hook, once inline in YAML), the two would drift over time and a
commit could pass locally but fail in CI (or vice versa) purely from copy-paste divergence —
not from the contributor doing anything wrong. One script, two callers, is the smaller diff and
removes that whole failure class.

**Alternatives considered**:
- Inline the regex separately in the GitHub Actions YAML — rejected: duplicates FR-002's rule
  in two languages/places with no mechanism keeping them in sync.
- Adopt a third-party commit-lint tool (e.g., a Node-based `commitlint`) — rejected: this is a
  Kotlin/Gradle project with no existing Node toolchain; introducing one solely for a regex
  check is a new dependency for something a 15-line POSIX shell script already does.

## Decision 2: CI validates every commit in the PR, not just the PR title

**Decision**: The CI check (FR-009) walks every commit in the pull request's compare range
(`git log <base>..<head> --format=%s`) and validates each header with the shared script from
Decision 1 — the same per-commit rule the local hook already enforces (FR-001–FR-003).

**Rationale**: A common alternative pattern (e.g., the `amannn/action-semantic-pull-request`
GitHub Action) validates only the **PR title**, on the assumption that the repo always
squash-merges — in that model, the individual commits disappear into one squashed commit whose
message IS the PR title, so only the title matters. This repo hasn't declared a merge strategy
(squash vs. merge-commit vs. rebase), and FR-002/FR-003 are already written in terms of
individual commit messages. Validating every commit is correct regardless of which merge
strategy is later chosen, whereas PR-title-only validation would silently under-enforce if
merge-commit or rebase-merge is ever used.

**Alternatives considered**:
- PR-title-only check via `amannn/action-semantic-pull-request` — rejected for the reason
  above (assumes squash-merge, which isn't a stated constraint).
- Validating only the most recent commit — rejected: contributors regularly push multiple
  commits before squash/rebase; skipping earlier ones defeats SC-001's "100% of commits"
  target.

## Decision 3: CI platform and trigger

**Decision**: A GitHub Actions workflow (`.github/workflows/commit-lint.yml`) triggered on
`pull_request` (opened, synchronize, reopened), running on `ubuntu-latest`, checking out with
enough history to diff against the PR base (`fetch-depth: 0`), then running the Decision 1/2
logic and failing the job (non-zero exit) on any non-conforming commit — which GitHub surfaces
as a failed required check blocking merge (FR-009).

**Rationale**: GitHub Actions is already available for any GitHub-hosted repo at no extra
setup cost — no new external service, matching the "no new dependency" constraint from the
original spec's Assumptions section.

**Alternatives considered**: A hosted third-party CI (CircleCI, etc.) — rejected, no other CI
is in use anywhere else in this project; would be a new external dependency for no added value.

## Resolved unknowns

No `NEEDS CLARIFICATION` markers remain in the Technical Context — this is a tooling/CI feature
with no runtime language, storage, or performance dimension beyond what's captured above.
