# DDR-0009: Milestone Display Unit Conversion

- **Date**: 2026-06-01
- **Status**: Accepted

## Context

M3 remediation finding #4 identified that milestone achievement labels and toast
notifications hard-coded `"lb"` (e.g. `"5 lb Milestone"`, `"5 lb milestone reached!"`).
This violates FR-P-1, which requires all weight values to display in the user's
preferred unit.

Milestone `threshold` values are **canonical pounds** — the weight-entry create handler
normalises both the goal and entry weights to lbs before comparing against thresholds
`(5, 10, 25, 50)` (FR-Ach-2).  The stored `threshold` is therefore always a pound value,
and milestone detection is create-only (ADR-0026).  The frontend thus needs to convert
from a **known constant source unit** (`'lbs'`) to the user's preferred unit at render
time; no backend change, API addition, or DB migration is required.

The affected surfaces:
- `AchievementsPage` — milestone row label (`{N} lb Milestone`)
- `AchievementNotification` — milestone toast copy (`{N} lb milestone reached!`)
- `GoalHistoryList` — goal history start → target values (separate finding, also fixed)
- `GoalFormWithPrefill` — goal-create prefill (separate finding, also fixed)

## Decision

**Convert milestone thresholds from canonical pounds (`MILESTONE_THRESHOLD_UNIT`) to the
user's preferred unit using `formatWeightInPreferredUnit` at the two milestone display
sites.**

- `MILESTONE_THRESHOLD_UNIT: WeightUnit = 'lbs'` is exported as a named constant from
  `features/achievements/schemas/achievement.ts` to document the invariant and avoid
  duplicating the magic string `'lbs'` at both display sites.
- `AchievementsPage` reads `usePreferences().preferences.weightUnit` and threads
  `preferredUnit` into a refactored `achievementLabel(ach, preferredUnit)` function.
- `AchievementNotification` receives `preferredUnit` as a **required prop** (keeping it
  presentational — the page/form supplies the context hook result).
  `WeightEntryFormPage` wires `preferences.weightUnit` to this prop.
- Copy standardisation: `"5 lb"` → `"5.0 lbs"` (lbs user) / `"2.3 kg"` (kg user).
  `formatWeightInPreferredUnit` appends the unit and formats to 1 decimal place,
  matching the number style used throughout the app.

## Rationale

**FR-P-1 compliance:** A kg-preferring user should see `"2.3 kg Milestone"`, not
`"5 lb Milestone"`.  The hard-coded `"lb"` label was accurate for the detection source
unit but violated the display unit preference.

**Reuse `formatWeightInPreferredUnit`:** The existing helper already handles conversion +
formatting at 1 dp and is used by the weight history table, dashboard, and goal summary.
Using it here keeps the number style consistent and avoids a new formatting branch.

**Detection unchanged:** Milestone detection logic and stored threshold values remain
pound-canonical.  This is a display-only change.

**Named constant:** `MILESTONE_THRESHOLD_UNIT` makes the source-unit contract explicit
at both call sites.  A future rename would be caught at compile time.

## Alternatives Considered

**Document-only (accept the "lb" label as intentional):** Rejected — violates FR-P-1.
A kg user would see the wrong unit on every milestone they earn.

**Dual notation `"5 lb (2.3 kg) Milestone"`:** Rejected — verbose, especially in a
6-second toast.  Showing only the preferred unit matches how the rest of the app presents
weight values.

**Backend unit field on `AchievementRecord`:** Rejected — the source unit is a
compile-time constant (`'lbs'`), not a per-row value.  Adding a backend field and
migration would add complexity with no information gain.

## Consequences

- **Copy change:** milestone labels and toasts now include 1 dp and the preferred unit
  (`"5.0 lbs Milestone"` / `"2.3 kg Milestone"`) instead of `"5 lb Milestone"`.
  This is intentional and documented here (D4 in the implementation spec).
- **Detection asymmetry accepted:** Whether the 5/10/25/50 lb tiers should be
  physically equivalent for kg users (e.g. "5 kg milestone" ≈ 11 lb, not 5 lb) is a
  product question deferred to a future iteration.  The current behaviour is that
  thresholds remain pound-denominated; kg users cross them at the same absolute weight
  as lbs users.  This asymmetry is a known, accepted, deferred concern.
- Two E2E assertions in `achievement-notification.spec.ts` updated from `/5 lb milestone/i`
  to `/5\.0 lbs milestone/i` to reflect the new copy.

## Related

- DDR-0007 — Achievement Notification UI (milestone copy superseded by this DDR)
- ADR-0019 — Preferences EAV storage
- ADR-0026 — Achievement write-flow contract (create-only, permanent)
- SRS FR-P-1 (`WeighToGo_Web_SRS_v2.md:661`)
- SRS FR-Ach-2 (`WeighToGo_Web_SRS_v2.md`) — `"pounds"` is correct for detection
- M3 quality review finding #4 (`docs/standards/M3_WEB_APP_QUALITY.md:89-101`)
