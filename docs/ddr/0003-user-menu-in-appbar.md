# DDR 0003: User Menu in AppBar

## Status
Accepted

## Context
Phase 7 adds the first user-visible session controls: display name, email, and logout. The app has an AppBar (MUI) at the top of every authenticated page via AppLayout. We need a home for these controls.

## Decision
Session controls (display name, email, logout) live in a user menu accessed from an avatar chip in the AppBar's right side (implemented as MUI Avatar + IconButton + Menu).

## Rationale
- Industry convention: Google, GitHub, Figma all put user session controls in a top-right avatar/menu
- Separates session actions from navigation routes — logout is not a page, it's an action
- Keyboard accessible via the existing MUI Menu component (arrow keys, Escape to close)
- Alternatives: NavList entry (pollutes navigation with an action), dedicated `/logout` route (breaks REST idiom — logout is a POST not a GET)

## Impact
- `src/components/AppLayout.tsx` — AppBar gains UserMenu on the right
- `src/components/UserMenu.tsx` — new component
- Only authenticated views are affected (AppLayout is gated behind ProtectedRoute)

## Visual Reference
Avatar chip (initials) in top-right of AppBar → click → dropdown menu shows display name (subtitle), email (secondary text), divider, Log out button.
