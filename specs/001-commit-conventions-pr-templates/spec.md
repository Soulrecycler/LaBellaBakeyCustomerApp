# Feature Specification: Conventional Commit Enforcement & GitHub PR Template

**Feature Branch**: `001-commit-conventions-pr-templates`
**Created**: 2026-07-10
**Status**: Draft
**Input**: User description: "add a precommit hook to ensure we add conventional commits and also add the github templates for which are used when we raise a pr on github"

## Clarifications

### Session 2026-07-10

- Q: SC-001 requires 100% of commits on shared branches to follow Conventional Commits, but a local commit-msg hook alone is bypassable (`--no-verify`) and not auto-installed on clone — should enforcement also include a server-side/CI check? → A: Yes — add a CI check (e.g., a GitHub Actions workflow) that validates commit messages on every pull request and blocks merge on failure, independent of the local hook.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Commit messages are checked before they're created (Priority: P1)

A contributor makes a change and runs `git commit`. Before the commit is finalized, their
commit message is checked against the Conventional Commits format. If it doesn't conform,
the commit is rejected on the spot with a clear explanation of what's wrong and an example
of a valid message, so they can fix it and commit again immediately.

**Why this priority**: This is the core of the request — without it, nothing enforces
consistent commit history. It's also the highest-leverage piece: a consistent commit log is
what makes changelogs, release notes, and semantic versioning possible later.

**Independent Test**: Can be fully tested by attempting a commit with a message like
`"fixed the bug"` (rejected, with guidance) and then `"fix: correct cart total rounding"`
(accepted) — delivers value on its own, independent of the PR template.

**Acceptance Scenarios**:

1. **Given** a contributor has staged changes, **When** they commit with a message that does
   not start with a recognized type (e.g., `feat`, `fix`, `docs`, `chore`, …), **Then** the
   commit is rejected and an error message explains the required format with an example.
2. **Given** a contributor has staged changes, **When** they commit with a properly formatted
   message (e.g., `feat(cart): add quantity stepper`), **Then** the commit succeeds with no
   extra friction.
3. **Given** a contributor is completing a normal Git operation that produces an
   auto-generated message (e.g., a merge commit), **When** the commit is created, **Then** it
   is not blocked by the validation.
4. **Given** a contributor's local commit-msg hook is missing or was bypassed, **When** they
   open a pull request containing a non-conforming commit message, **Then** an automated CI
   check fails and blocks the pull request from merging until the message is fixed.

---

### User Story 2 - Opening a PR starts from a consistent template (Priority: P2)

A contributor pushes their branch and opens a new pull request on GitHub. The PR description
field is pre-filled with a standard template prompting them for a summary, the motivation,
how the change was tested, and any related spec/issue — so reviewers get consistent,
comparable information on every PR without having to ask for it.

**Why this priority**: Independently valuable — it improves review quality and consistency
even without the commit hook — but ranked below P1 because it affects review-time
communication rather than the integrity of the commit history itself.

**Independent Test**: Can be fully tested by opening a new PR against this repository on
GitHub and confirming the description field is pre-populated with the template content,
independent of any commit-message behavior.

**Acceptance Scenarios**:

1. **Given** a contributor opens a new pull request against this repository on GitHub,
   **When** the PR creation page loads, **Then** the description field is pre-filled with the
   standard template (summary, motivation/why, testing performed, related spec/issue link).
2. **Given** the template is pre-filled, **When** the contributor submits the PR without
   removing the template's required sections, **Then** the resulting PR description retains
   those sections so reviewers can find the information.

### Edge Cases

- What happens when a commit is a merge commit, revert commit, autosquash marker
  (`fixup!` / `squash!`), or other Git-generated message the contributor didn't type by hand?
  These MUST NOT be blocked by validation (see FR-003).
- What happens when the local hook is missing (not installed on that clone) or bypassed
  (`--no-verify`)? The pull request's CI check still validates every commit message and blocks
  merge until fixed (see FR-009).
- What happens when a contributor amends or rewrites an existing commit message (`git commit
  --amend`, interactive rebase)? The amended message is re-checked the same way a new commit
  would be.
- What happens when a PR is created through a non-browser flow (e.g., a CLI tool) that
  doesn't render the template automatically? Out of scope for this feature — the requirement
  is that GitHub's own PR creation flow picks up the template; tools that bypass it are not
  covered.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The repository MUST validate every commit message against the Conventional
  Commits format before the commit is finalized.
- **FR-002**: Validation MUST accept the standard Conventional Commits types: `feat`, `fix`,
  `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`, `revert`, each
  optionally followed by a parenthesized scope (e.g., `feat(cart): ...`).
- **FR-003**: Validation MUST NOT block Git-generated or Git-workflow commit messages that a
  contributor did not author as a normal message — specifically merge commits, revert commits,
  and autosquash markers (`fixup!` / `squash!` prefixes). This list is illustrative of the
  category, not exhaustive.
- **FR-004**: When a commit message fails validation, the repository MUST reject the commit
  and present a clear, actionable message explaining the required format and showing at least
  one valid example, before the commit is created.
- **FR-005**: Commit-message validation MUST run locally, before the commit is created, so a
  contributor gets feedback in the same action rather than after pushing.
- **FR-006**: The repository MUST provide a default pull request template that GitHub
  automatically applies to the description field whenever a new PR is opened against this
  repository.
- **FR-007**: The pull request template MUST prompt the author for, at minimum: a summary of
  the change, the motivation/why, how the change was tested, and a link to any related
  spec/issue.
- **FR-008**: The pull request template MUST remain in place for all future PRs against this
  repository without per-PR setup by the author.
- **FR-009**: The repository MUST enforce the Conventional Commits format via an automated
  check that runs on every pull request, independent of whether the contributor's local
  commit-msg hook is installed or was bypassed, and MUST block the pull request from merging
  when any commit message fails validation.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of commits that reach the repository's shared branches follow the
  Conventional Commits format (the aggregate outcome; guaranteed by the combination of SC-002
  locally and SC-005 at the PR gate).
- **SC-002**: A contributor who mistypes a commit message gets rejection feedback within the
  same commit attempt — zero non-conforming commits are ever created locally.
- **SC-003**: 100% of new pull requests opened against the repository start from the standard
  template, verified by the template's section headers being present in the PR description at
  creation time.
- **SC-004**: Reviewers can find a summary, motivation, and testing description in every new
  PR without requesting it separately.
- **SC-005**: The bypass-resistance guarantee — a non-conforming commit message is caught and
  blocks merge at the PR gate before it reaches a shared branch, even when a contributor's
  local hook was skipped, not installed, or bypassed (the specific mechanism that makes
  SC-001's aggregate "100%" achievable rather than best-effort).

## Assumptions

- No commit history convention exists yet in this repository, so the standard Conventional
  Commits type list (`feat, fix, docs, style, refactor, perf, test, build, ci, chore, revert`)
  is adopted as-is, with no project-specific types.
- A single default PR template is sufficient for now; separate templates per change type
  (e.g., bug vs. feature) are not required until the project has enough PR volume to justify
  them.
- Enforcement applies repo-wide (not scoped to a specific app/server subfolder), since no
  application code exists yet.
- Contributors use a local Git client that supports standard Git hooks (any current version of
  Git on macOS, Linux, or Windows).
