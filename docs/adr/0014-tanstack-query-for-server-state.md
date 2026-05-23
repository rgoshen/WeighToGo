# ADR-0014: TanStack Query for Server State

- **Date**: 2026-05-23
- **Status**: Accepted

## Context

Phase 7 introduces auth hydration, mutation state, and server cache invalidation on
the frontend. The existing `AuthContext` uses plain `useState`/`useEffect` to fetch
the current user (`/api/v1/auth/me`). That approach provides no cache control, no
automatic refetch-on-focus, no standard mutation pattern, no request deduplication,
and no background revalidation.

The frontend already declares `@tanstack/react-query` v5 in `package.json`; it is
present in the lock file but has not yet been wired into `main.tsx` with a
`QueryClientProvider`.

## Decision

Use TanStack Query v5 as the single source of truth for all server-fetched state,
starting with `useQuery(['auth', 'me'])` inside `AuthContext`. The `QueryClient`
instance is created once in `main.tsx` and the `QueryClientProvider` wraps the
entire app tree.

## Rationale

TanStack Query is already installed, so this decision adds no new dependency weight.
It satisfies the SRS §7 requirement for robust error handling and §10 requirement for
a clean separation of server state from UI state. Choosing a consistent pattern now
prevents ad-hoc `useEffect`/`useState` proliferation across future phases (weight
entries, goals, profile).

The alternatives were not chosen for the reasons described below.

## Consequences

- **Positive**:
  - All server state flows through `QueryClient` — consistent loading, error, and
    stale-while-revalidate semantics across the app.
  - `useMutation` provides a standard pattern for login, logout, and registration
    with optimistic updates and rollback if needed.
  - Cache invalidation on logout clears stale auth data automatically.
  - Future phases (weight entries, goals) follow the same pattern without further
    architectural decisions.
- **Negative**:
  - `main.tsx` must be updated to wrap the app in `QueryClientProvider` before Phase
    7 components can use `useQuery`.
  - Developers new to TanStack Query need to learn the `QueryClient`/`QueryKey`
    mental model.
- **Follow-ups**:
  - ADR-0015 (if needed) for any React Query Devtools or persistence plugin choices.
  - All subsequent phase ADRs that introduce new server-fetched resources should
    reference this ADR as the governing pattern.

## Alternatives Considered

- **Plain `useState`/`useEffect` hooks** — already in use in `AuthContext`. No cache,
  no refetch-on-focus, no mutation tracking, duplicated fetch logic per component.
  Ruled out: does not scale to the full app.
- **Redux Toolkit Query (RTK Query)** — full-featured but heavier; adds Redux
  dependency and a different mental model. Not already installed; no benefit over
  TanStack Query for this project's size.
- **SWR** — similar cache/revalidate capability but TanStack Query is already in
  the lock file and has a stronger mutation API (`useMutation`) that aligns with
  the auth write operations in Phase 7.
