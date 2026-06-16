# DDR-0010: Split-Screen Landing (Login + Registration)

- **Date**: 2026-06-15
- **Status**: Accepted

## Context

Logged-out visitors landed on a login-only screen with no visible path to create an account; registration required knowing the `/register` URL. The application root should present both actions side by side so a first-time visitor can register without hunting for the link.

## Decision

Render a two-pane split-screen at the root `/` for unauthenticated users (`LandingPage`):

- **Left pane — "Log In"**: the existing `LoginForm`.
- **Right pane — "Create Account"**: the existing `RegisterForm`.
- App branding (`Weigh to Go!`, `h1`) sits above the two panes.
- Each pane is a MUI `Paper` rendered as a `section` with an accessible name; the panes sit in a flex row on `md`+ and stack into a single column on `xs`–`sm`.

## Rationale

Reusing the existing form components keeps validation, error display, and submit behavior identical to the dedicated `/login` and `/register` pages — the landing is pure layout. A two-column-to-stacked responsive pattern is the conventional shape for a combined auth screen and reuses the project's MUI breakpoints and 44×44 touch-target theme. The split surfaces registration at the moment of first contact, which is the discoverability problem this addresses.

## Impact

- New component `LandingPage` (`web/frontend/src/features/auth/pages/LandingPage.tsx`).
- Rendered by `ProtectedRoute` at `/` for unauthenticated users (see ADR-0027).
- After logout the user returns to this landing.

**Accessibility:**
- One `h1` (brand) and two `h2` ("Log In", "Create Account"); tab order is login pane then register pane (DOM order).
- The two forms share "Email"/"Password" labels, so each pane exposes an ARIA `region` (`aria-labelledby` → its `h2`). This makes the **accessible region names "Log In" and "Create Account" a stable contract** referenced by the component's unit tests and the Playwright e2e selectors — changing the heading text is a cross-layer change.
- The landing is its own single `<main>`. Note: the *authenticated* dashboard already nests `<main>` (`AppLayout` + `DashboardPage`); that is pre-existing, untouched by this change, and tracked as a separate a11y follow-up.

## Visual Reference

```
                       Weigh to Go!
   ┌────────────────────────┬────────────────────────┐
   │  Log In                │  Create Account         │
   │  [ Email            ]  │  [ Display name      ]  │
   │  [ Password         ]  │  [ Email             ]  │
   │  [   Log in   ]        │  [ Password          ]  │
   │                        │  [ Confirm password  ]  │
   │                        │  [   Create account  ]  │
   └────────────────────────┴────────────────────────┘
   (panes stack into one column on narrow viewports)
```
