# Phase 1 Data Model: Conventional Commit Enforcement & GitHub PR Template

No data entities. This feature is repo tooling/CI configuration (a git hook, a GitHub Actions
workflow, and a static PR description template) — it has no persisted domain model, database,
or runtime data structures. The spec's own "Key Entities" section was omitted for the same
reason (see `spec.md`).

The only "shape" worth naming is the commit-message header format itself, which is a contract
(see Decision 1/2 in `research.md`), not a data entity:

```
<type>(<scope>): <description>
```

- `type`: one of `feat, fix, docs, style, refactor, perf, test, build, ci, chore, revert`
- `scope`: optional, `[a-zA-Z0-9_.-]+`
- `description`: required, free text
