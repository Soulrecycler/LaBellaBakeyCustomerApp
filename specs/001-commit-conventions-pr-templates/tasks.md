---

description: "Task list template for feature implementation"
---

# Tasks: Conventional Commit Enforcement & GitHub PR Template

**Input**: Design documents from `/specs/001-commit-conventions-pr-templates/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, quickstart.md

**Tests**: Not formally requested in the spec. One lightweight shell self-check is still
included (T005) per this project's Ponytail rule that non-trivial branch/regex logic needs
at least one runnable check — it is not a TDD contract-test suite.

**Organization**: Tasks are grouped by user story (spec.md) so each can be delivered and
verified independently.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependency on an incomplete task)
- **[Story]**: US1 or US2, per spec.md priorities
- Exact file paths are included in every description

## Already done this session (context, not tasks)

`.githooks/commit-msg` (regex inline), `.github/PULL_REQUEST_TEMPLATE.md`, and the
`installGitHooks` Gradle task already exist and were manually verified (bad message
rejected, good message accepted). What's left is extracting the shared validator, adding
the CI check from `/speckit-clarify` (FR-009), and verification passes below.

---

## Phase 1: Setup

**Purpose**: Confirm existing groundwork and prepare the new CI workflow location.

- [X] T001 Verify `.githooks/commit-msg` exists and `git config core.hooksPath` is set to
      `.githooks` (via the `installGitHooks` Gradle task in `build.gradle.kts`); re-run
      `./gradlew installGitHooks` if not
- [X] T002 [P] Create the `.github/workflows/` directory at the repo root for the new CI
      workflow

---

## Phase 2: Foundational

**Purpose**: Blocking prerequisites shared by all user stories.

None. User Story 1 (commit validation) and User Story 2 (PR template) are fully independent
— neither needs shared groundwork beyond ordinary Phase 1 setup.

---

## Phase 3: User Story 1 - Commit messages are checked before they're created (Priority: P1) 🎯 MVP

**Goal**: Every commit message is checked against Conventional Commits — locally (fast
feedback) and via a required CI check on the PR (guaranteed enforcement even if the local
hook is missing or bypassed, per FR-009 / `/speckit-clarify`).

**Independent Test**: Locally, `git commit -m "fixed the bug"` is rejected with guidance;
`git commit -m "fix: correct cart total rounding"` succeeds. On GitHub, open a PR containing
one commit with a bad message and confirm the `commit-lint` check fails and blocks merge;
fix the message and confirm the check turns green.

### Implementation for User Story 1

- [X] T003 [US1] Extract the Conventional Commits regex/type-list and error text out of
      `.githooks/commit-msg` into a new shared script, `.githooks/check-commit-message.sh`,
      taking one commit-message header string as `$1` and exiting 0 on match / 1 with the
      FR-004 error text on mismatch (research.md Decision 1)
- [X] T004 [US1] Update `.githooks/commit-msg` to extract the header line and delegate to
      `.githooks/check-commit-message.sh` instead of containing the regex itself, preserving
      the merge/revert/fixup/squash bypass (FR-003) — depends on T003
- [X] T005 [P] [US1] Add `.githooks/check-commit-message.test.sh`: a small shell self-check
      asserting the shared validator accepts examples like `feat(cart): add quantity stepper`
      and rejects examples like `fixed the bug` — depends on T003 (different file, but
      exercises the script T003 creates, so run after it)
- [X] T006 [US1] Add `.github/workflows/commit-lint.yml`: trigger on `pull_request`
      (`opened`, `synchronize`, `reopened`); checkout with `fetch-depth: 0`; run
      `git log ${{ github.event.pull_request.base.sha }}..${{ github.sha }} --format=%s`;
      call `.githooks/check-commit-message.sh` on each header, skipping Git-generated
      messages per FR-003; fail the job on the first non-conforming header (FR-009) —
      depends on T003
- [X] T007 [US1] Verify the refactored local hook end-to-end (regression check after T004's
      refactor): with `core.hooksPath=.githooks`, confirm `git commit -m "fixed the bug"` is
      rejected with the FR-004 guidance and `git commit -m "fix: correct cart total rounding"`
      is accepted, plus a merge/`fixup!` message passes untouched (FR-001–FR-005) — depends on T004
- [ ] T007a [US1] Manually verify per quickstart.md: open a real PR with one intentionally
      bad commit message, confirm the `commit-lint` check fails and blocks merge; amend the
      message, confirm the check turns green (FR-009) — depends on T004, T006

**Checkpoint**: Local hook and CI both enforce Conventional Commits; User Story 1 is fully
functional and independently testable.

---

## Phase 4: User Story 2 - Opening a PR starts from a consistent template (Priority: P2)

**Goal**: Every new PR's description is pre-filled with the standard template.

**Independent Test**: Open a new PR against this repository on GitHub and confirm the
description field is pre-populated with Summary / Motivation / Testing / Related spec-issue.

### Implementation for User Story 2

- [X] T008 [US2] Verify `.github/PULL_REQUEST_TEMPLATE.md` exists at GitHub's recognized path
      (satisfying FR-006/FR-008 automatically for all future PRs) and contains all four
      sections required by FR-007 (Summary, Motivation, Testing, Related spec/issue); adjust
      wording only if a section is missing
- [ ] T009 [US2] Manually verify per quickstart.md: open a real PR against this repository
      and confirm GitHub pre-fills the description from the template (FR-006, FR-008)

**Checkpoint**: Both user stories are independently functional.

---

## Phase 5: Polish & Cross-Cutting Concerns

- [ ] T010 [P] Run `quickstart.md` end-to-end on a fresh clone: `./gradlew installGitHooks` →
      bad commit rejected → good commit accepted → PR opens pre-filled → CI catches a
      deliberately bad commit even with the hook uninstalled
- [ ] T011 Update `specs/001-commit-conventions-pr-templates/checklists/requirements.md` if
      verification (T007/T007a/T009/T010) surfaces any gap against the checklist items

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
- **Foundational (Phase 2)**: Empty — nothing blocks either story
- **User Story 1 (Phase 3)**: Depends on Phase 1 (needs `.github/workflows/` to exist for T006)
- **User Story 2 (Phase 4)**: Depends on Phase 1 only — fully independent of Phase 3
- **Polish (Phase 5)**: Depends on both user stories being complete

### Within User Story 1

T003 → T004, T005, T006 (all read the script T003 creates) → T007 (re-verifies the refactored
local hook, depends on T004) and T007a (verifies the CI/PR path, depends on T004 + T006)

### Parallel Opportunities

- T001 and T002 (Setup) can run in parallel
- T005 can run in parallel with T004 and T006 once T003 is done (different files)
- User Story 1 (Phase 3) and User Story 2 (Phase 4) can be worked on in parallel by different
  people — they touch entirely disjoint files

---

## Parallel Example: User Story 1

```bash
# After T003 (check-commit-message.sh) is done, run together:
Task: "Update .githooks/commit-msg to delegate to check-commit-message.sh"
Task: "Add .githooks/check-commit-message.test.sh self-check"
Task: "Add .github/workflows/commit-lint.yml CI check"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 3: User Story 1 (commit validation, local + CI)
3. **STOP and VALIDATE**: run T007's manual PR check
4. User Story 1 alone already satisfies the highest-priority requirement (FR-001–FR-005,
   FR-009) even before the PR template is touched

### Incremental Delivery

1. Setup → Foundation ready (nothing to build in Phase 2)
2. Add User Story 1 → verify independently → this is the MVP
3. Add User Story 2 → verify independently → both stories now complete
4. Polish: run the full quickstart.md end-to-end

---

## Notes

- [P] tasks touch different files with no dependency on an incomplete task
- Both user stories touch entirely disjoint files (`.githooks/*` + `.github/workflows/*` vs.
  `.github/PULL_REQUEST_TEMPLATE.md`), so there is no cross-story coupling to protect
- No contract tests — this feature has no contracts/ (purely internal tooling, per plan.md)
- Commit after each task or logical group, using the very hook this feature adds
