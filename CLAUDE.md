<!-- SPECKIT START -->
For additional context about technologies to be used, project structure,
shell commands, and other important information, read the current plan:
`specs/001-commit-conventions-pr-templates/plan.md`
<!-- SPECKIT END -->

## Git & push policy (permanent)

- **Push identity**: All pushes to `origin` MUST be made as the **`Soulrecycler`** GitHub
  account — never as `spereira-cci`. Before any `git push`, verify the active credential
  resolves to `Soulrecycler` (e.g. `ssh -T git@github.com` says "Hi Soulrecycler!", or the
  HTTPS token belongs to `Soulrecycler`). If it would push as `spereira-cci`, STOP and fix
  auth first — do not push.
- **Branch model**: `main` = production, `develop` = integration (features merge here),
  feature branches = per-feature work (Spec Kit creates these). Never push directly to
  `main` or `develop`.
- **PR-first**: Every change reaches `develop` or `main` through a pull request, never a
  direct push/merge to those branches — including the initial project setup.

## Architecture rule (permanent)

- Every implementation, feature, refactor, bug fix, and planning session must strictly
  follow the architecture defined in `.specify/memory/architecture.md`.
- Before generating code, always consult `.specify/memory/architecture.md`.
- All planning must follow the Ponytail methodology using the Ponytail plugin.
- Every implementation plan should conform to the established architecture before coding
  begins.
- If a request conflicts with the architecture, explain the conflict and propose an
  architecture-compliant solution instead of violating the architecture.
