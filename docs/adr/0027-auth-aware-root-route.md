# ADR-0027: Auth-Aware Root Route with Public Split-Screen Landing

- **Date**: 2026-06-15
- **Status**: Accepted

## Context

The root path `/` was a protected route rendering `DashboardPage` ("Primary landing page after login"). A logged-out visitor at `/` was redirected to `/login`, a login-only page; registration lived at a separate `/register` URL with no link from the login screen. New users had to know to navigate to `/register` manually — a discoverability gap for a portfolio-quality app.

The goal (issue #153): present a split-screen at the application root — login on the left, registration on the right — for logged-out visitors, while preserving the dashboard at `/` for authenticated users. The change had to stay within the existing React Router v7 architecture (a single `AppLayout` layout-route group gated by a `ProtectedRoute` component) and must not alter the API.

## Decision

Make `/` **auth-aware** by **extending the existing `ProtectedRoute` gate in place** rather than restructuring the routes. `/` remains the `index` of the existing `<Route element={<ProtectedRoute><AppLayout/></ProtectedRoute>}>` group. `ProtectedRoute` gains one branch:

- initial `/me` probe in flight → loading splash;
- authenticated → the protected children (the `AppLayout` shell, whose `<Outlet/>` renders the matched page, including the dashboard index);
- unauthenticated **at `/`** → the new public `LandingPage` (split-screen login + registration);
- unauthenticated on any other protected path → redirect to `/login?from=<path>` (unchanged).

`LandingPage` composes the existing `LoginForm`/`RegisterForm` and `useLogin`/`useRegister`, so no auth logic is duplicated. Dedicated `/login` and `/register` routes are retained. After **logout**, the user returns to `/` (the canonical entry) rather than `/login`.

## Rationale

Extending `ProtectedRoute` keeps `/` as the index of the shared layout group, so a single `AppLayout` instance persists across navigation between the dashboard and other protected pages — no shell remount. It introduces no new route, no new component API, and leaves the route tree unchanged: the smallest diff that satisfies the requirement and honors the repo priority order (Security → Accessibility → Readability → Maintainability → Modularity → Optimization). Reusing the form components and hooks keeps the change DRY and confines it to the frontend; the same `/api/v1/auth/*` endpoints are used unchanged.

## Consequences

- **Positive**: registration is discoverable from the application root; entry (`/`) and exit (logout → `/`) are symmetric; no API/schema/migration impact; no shell remount; minimal surface area.
- **Negative**: `ProtectedRoute` now renders one public view (the landing at `/`), so its name slightly under-describes its behavior — mitigated by an explicit docstring. Login and registration are reachable from two surfaces (`/` and the dedicated `/login`/`/register`), accepted below.
- **Follow-ups**: the authenticated dashboard nests `<main>` (`AppLayout` + `DashboardPage`); this is pre-existing and out of scope here — track as a separate accessibility issue.

## Alternatives Considered

- **Separate top-level `/` route** rendering its own `<AppLayout><DashboardPage/></AppLayout>` for authenticated users — rejected: a second `AppLayout` instance remounts the shell (AppBar/Drawer, scroll, effects) on every navigation between `/` and other protected pages.
- **Give `AppLayout` a `children` prop** to support the above — rejected: unnecessary new public API; the extend-in-place approach needs no `AppLayout` change.
- **Move the dashboard to `/dashboard`, make `/` a public landing** — rejected: churns the SRS route table and every post-login/`from=` redirect target.
- **Enhance `/login` into the split screen, leave `/` redirecting** — rejected: the canonical entry URL would be `/login`, not the root the user asked for.
- **Redirect `/register` (and `/login`) to `/`** to remove the dual surface — rejected: `/login` is required as the `?from=` post-redirect target, and dedicated auth URLs remain useful for deep links and password managers; the surfaces share components, so there is no logic duplication.
