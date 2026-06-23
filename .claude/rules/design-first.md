# Design-First Workflow — Mandatory

## Rule

Before implementing ANY feature, bug fix with architectural impact, or significant change, you MUST:

1. **Write a design doc first** in `docs/` (e.g., `docs/feature-xxx-design.md`)
2. **Present the design to the user for approval** — do NOT proceed to implementation until the user explicitly approves
3. **Only after approval**, begin implementation following the approved design

## Design Doc Requirements

Every design doc MUST include at minimum:

- **Background** — what exists today, why this change is needed
- **Goal** — what we want to achieve
- **Approach** — how we will do it
- **Detailed design** — database schema changes, API contracts, component interactions
- **Workflow diagrams** — use Mermaid for complex logic flows
- **Test plan** — what tests will be written (tests are written before implementation per project standard)

## What Requires a Design Doc

- New features or capabilities
- Changes to database schema
- New or modified API endpoints
- Changes to FSM states or transitions
- New integrations or protocol support
- Architectural refactors
- Any change spanning multiple components

## What Does NOT Require a Design Doc

- Typo fixes, comment updates, formatting
- Single-line bug fixes with obvious root cause
- Dependency version bumps
- Test-only changes (adding tests for existing code)

## Approval Gate

- After writing the design doc, explicitly ask the user: "Please review the design doc. Should I proceed with implementation?"
- If the user requests changes, update the design doc and re-present for approval
- Do NOT start writing implementation code (including tests) until the user says to proceed
- The design doc serves as the source of truth for the implementation scope — do not add features beyond what was approved
