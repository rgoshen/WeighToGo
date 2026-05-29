# DDR-0005: Goal Progress Visualization

- **Date**: 2026-05-28
- **Status**: Accepted

## Context

FR-D-4 requires a progress bar showing percentage completion toward the active goal. The GoalProgress value object from the domain layer exposes a `percent` float in [0, 100]. The UI must handle three states:

1. **No active goal** — user has not set a goal yet.
2. **Active goal, no weight entries** — progress is indeterminate (backend returns `progress_percent: null`).
3. **Active goal with entries** — progress is a known percentage.

The component appears in two places: the Goals page (primary home) and the Dashboard (summary card replacing the placeholder).

NFR-A-4 requires minimum 4.5:1 contrast on text, and NFR-A-6 requires `prefers-reduced-motion` to be honoured.

## Decision

Use MUI `LinearProgress` in `variant="determinate"` mode with a numeric label below the bar. Specific choices:

- **Component:** `LinearProgress` from `@mui/material` (already a project dependency; no new library).
- **Variant:** `determinate` when progress is known; `buffer` mode is explicitly **not** used — the "no entries yet" state shows a zero-value bar with a helper text, not an animated indeterminate bar.
- **Percent label:** displayed as `{percent.toFixed(0)}%` in a `Typography` element below the bar; right-aligned. Accessible because the bar also carries `aria-valuenow`, `aria-valuemin="0"`, `aria-valuemax="100"`, and `aria-label="Goal progress"`.
- **No-goal state:** renders a `Typography` call-to-action — "Set a goal to track your progress" — rather than an empty or greyed-out bar. The CTA links to the goal creation form.
- **Motion:** `LinearProgress` animates by default. The component wraps the bar in `sx={{ transition: prefers-reduced-motion ? 'none' : undefined }}` — MUI v9 honours `prefers-reduced-motion` natively via its theme animations toggle.

## Rationale

- **LinearProgress over custom SVG arc:** keeps the implementation within the existing MUI component set (no new dependency), benefits from MUI's built-in ARIA and colour-system integration, and is consistent with MUI's own progress-tracking UI examples.
- **Determinate-only (no indeterminate animation):** an indeterminate spinner when no entries exist would mislead the user into thinking data is loading. A static zero-value bar with "No entries yet" helper text is more honest about the data state.
- **Text label in addition to the bar:** the WCAG 1.4.1 "Use of Color" criterion requires that information not be conveyed by colour alone. A numeric percentage label means the progress is readable without the ability to distinguish the filled vs. unfilled colours.
- **No `buffer` variant:** buffer mode is intended for streaming/buffered media; it adds visual complexity (ghost fill) that is semantically misleading in a weight-tracking context.

## Impact

- `web/frontend/src/features/goals/components/GoalProgressBar.tsx` — new component implementing the decisions above.
- `web/frontend/src/features/goals/pages/GoalsPage.tsx` — consumes `GoalProgressBar`.
- `web/frontend/src/features/dashboard/components/GoalProgressCard.tsx` — new card consuming `GoalProgressBar`; replaces `GoalProgressPlaceholderCard` in `DashboardPage.tsx`.
- `web/frontend/src/features/dashboard/components/GoalProgressPlaceholderCard.tsx` — deleted.

## Visual Reference

```
Goal Progress               [48%]
████████████░░░░░░░░░░░░░░  48%
Start: 200 lbs → Target: 150 lbs

No active goal state:
[Set a goal to track your progress →]
```

State table:

| State | Bar | Label |
|---|---|---|
| No active goal | Hidden | CTA link |
| Active goal, no entries | value=0, colour muted | "No entries yet" |
| Active goal with entries | value=percent | "{percent}%" |
