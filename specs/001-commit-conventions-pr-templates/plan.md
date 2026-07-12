# Implementation Plan: Conventional Commit Enforcement & GitHub PR Template

**Branch**: `001-commit-conventions-pr-templates` | **Date**: 2026-07-10 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/001-commit-conventions-pr-templates/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Enforce Conventional Commits on every commit message, at two layers: a local `commit-msg` git
hook (fast, immediate feedback, FR-001–FR-005) and a GitHub Actions check on every PR
(guarantees the rule even if the local hook is missing or bypassed, FR-009 — added during
`/speckit-clarify`). Both layers share one validation script so there is a single source of
truth for the accepted format. Also add a default GitHub PR template (FR-006–FR-008) that
pre-fills every new PR's description with Summary / Motivation / Testing / Related spec-issue.
The local hook and PR template already exist from earlier work this session; this plan covers
what's left — extracting the shared validation script and adding the CI workflow — plus
documents the design for the parts already built.

## Technical Context

**Language/Version**: POSIX shell (hook + shared validation script), YAML + `bash` steps (GitHub Actions workflow)
**Primary Dependencies**: None new — `git` (already required) and GitHub Actions (built into any GitHub-hosted repo); no Node/commitlint or other package added (see research.md Decision 1)
**Storage**: N/A
**Testing**: A small shell self-check script exercising valid/invalid/merge-generated example messages against the shared validator (see Constitution Check — Ponytail's "one runnable check" rule for non-trivial branch/regex logic); CI workflow itself is verified by opening a real test PR (no local GitHub Actions runner available)
**Target Platform**: Contributor machines (macOS/Linux/Windows via Git) for the local hook; `ubuntu-latest` GitHub Actions runner for the CI check
**Project Type**: Repo tooling / CI configuration — not application code; doesn't touch the KMP app modules
**Performance Goals**: N/A (a regex match over a handful of commit headers per PR)
**Constraints**: No new external dependency or service; one canonical definition of "valid conventional commit" shared by both enforcement layers (research.md Decision 1)
**Scale/Scope**: Repo-wide, applies to every future commit and PR — not scoped to any app/server subfolder (per spec Assumptions)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

`.specify/memory/constitution.md` is still the unfilled template (no ratified project
principles recorded yet) — there are no concrete constitution gates to evaluate against.

`CLAUDE.md`'s permanent rule requires consulting `.specify/memory/architecture.md` before
generating code. That document defines the Clean Architecture/module structure for the
**Kotlin Multiplatform customer app** (`composeApp`, `core:*`, `feature:*`). This feature is
repo-level tooling (git hooks, a GitHub Actions workflow, a PR template) — it doesn't add,
change, or touch any KMP module, so `architecture.md` doesn't apply here and there is no
conflict to resolve.

**Result**: PASS (no violations; no applicable gates beyond the above).

## Project Structure

### Documentation (this feature)

```text
specs/001-commit-conventions-pr-templates/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md         # Phase 1 output (/speckit-plan command) — no entities; documents the commit-message contract
├── quickstart.md        # Phase 1 output (/speckit-plan command)
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

No `contracts/` directory — this feature is purely internal tooling with no library/API/CLI
surface exposed to other systems (per the plan workflow's explicit skip guidance).

### Source Code (repository root)

```text
.githooks/
├── commit-msg                    # existing — git hook entrypoint; delegates header check to check-commit-message.sh
├── check-commit-message.sh       # NEW — shared validator (single source of truth, research.md Decision 1)
└── check-commit-message.test.sh  # NEW — shell self-check for the shared validator (Ponytail "one runnable check" rule)

.github/
├── PULL_REQUEST_TEMPLATE.md      # existing — Summary / Motivation / Testing / Related spec-issue (FR-006–FR-008)
└── workflows/
    └── commit-lint.yml           # NEW — CI check (FR-009); walks PR commits, calls check-commit-message.sh per commit

build.gradle.kts                  # existing — `installGitHooks` task (already wired, sets core.hooksPath)
```

**Structure Decision**: Everything lives at the repo root under `.githooks/` and `.github/`,
consistent with where Git and GitHub already look for hooks and workflows respectively — no
new top-level directory is introduced. The shared validator is a plain script, not a new
module/package, since it has exactly two callers and no independent versioning need.

## Complexity Tracking

*No constitution violations — this section is not applicable.*
