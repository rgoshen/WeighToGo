# ADR-0023: TTL-Based Server-Side Caching Strategy

- **Date**: 2026-05-29
- **Status**: Accepted

## Context

NFR-P-5 (SRS v2 §7.2) calls for computed values that are expensive and stable —
the recent rate-of-change and milestone counters — to be cached server-side with
a documented TTL.

The expensive read in the web rebuild is the dashboard summary. Every load of
`GET /api/v1/dashboard/summary` runs `BuildDashboardSummary.execute`, which:

- reads the user's full active weight series over the `(user_id,
  observation_date)` index,
- normalises every entry to a canonical unit,
- computes the weekly rate of change, and
- materialises the full trend series.

The `DashboardSummary` read model already carries the two values NFR-P-5 names:
`rate_of_change` (the weekly rate) and `total_entries` (the count, plus the
trend length — the "counter"-style figures). These figures are stable between
weight-entry writes: they only change when the user adds, edits, or removes an
entry.

Constraints shaping the design:

- The cache must respect the `shared` import contract
  (`pyproject.toml`): a module under `weighttogo.shared` may not import any
  bounded context (`auth`, `goals`, `weight_tracking`, `achievements`,
  `preferences`) and the inner layers may not import frameworks. The cache must
  therefore be a pure, generic, framework-free data structure.
- It must be a Milestone Three "algorithms and data structure" artifact: a small,
  fully unit-tested structure with documented time and space complexity, mirroring
  the existing `shared/units.py` pure-function pattern.
- Staleness must be bounded and documented.

## Decision

Add a generic in-memory TTL cache, `TTLCache[K, V]`, in
`weighttogo/shared/cache.py`, and use it to memoize the per-user
`DashboardSummary`.

**What is cached.** The whole `DashboardSummary` value object, keyed by the
integer `user_id`. Caching the composed summary — rather than the rate and the
counter separately — is DRY: one entry covers both NFR-P-5 named values *and*
the trend computation that dominates the cost, with one invalidation point.

**TTL value.** **30 seconds**, exposed as the named constant
`DEFAULT_TTL_SECONDS = 30.0` (no magic number). Thirty seconds bounds any stale
read to seconds — short enough that the value is genuinely "stable" within the
window — while absorbing the burst of repeated dashboard reads a single page
session produces. It is a `[SHOULD]` performance optimisation, so a small bounded
staleness is an acceptable trade for avoiding repeated full-series reads.

**Expiry data structure.** A `dict` mapping `key -> _Entry(value, expires_at)`,
where `expires_at` is a `float` deadline taken from `time.monotonic()` at write
time plus the TTL. Expiry is **lazy**: a `get` that finds an entry past its
deadline deletes it and reports a miss. There is no background sweeper (YAGNI).
A monotonic clock is used, not wall-clock time, so the TTL is immune to system
clock adjustments. The `now` source is injectable, which makes expiry tests
deterministic without sleeping.

The expiry comparison uses `now >= deadline` — a magnitude comparison, never a
float-equality test. Because the deadline was derived by adding the TTL to a
reading of the same monotonic clock, the comparison is exact at the boundary.

**Complexity.** `get`, `set`, and `invalidate` are O(1) average — a single
`dict` operation each. Space is O(k) where k is the number of distinct live
keys (one per active user in the worker process).

**Invalidation triggers.** A successful weight-entry **create** invalidates the
requesting user's cache key. Detection runs at the composition root: the
weight-entries router (interface layer) calls
`invalidate_dashboard_cache(user_id)` after a successful create. The cache
object and its accessors live in the dashboard interface layer; both routers are
interface-layer modules, so this cross-context interface import respects the
import contracts (the dashboard router already imports the `auth` and `goals`
interfaces).

## Rationale

- **Cache the composed read model, not the parts.** One entry, one TTL, one
  invalidation point covers both NFR-P-5 figures and the trend. Splitting them
  would duplicate expiry bookkeeping for no benefit (DRY, KISS).
- **Pure, generic structure in `shared`.** Keeping `TTLCache` free of any
  bounded-context or framework import preserves the Clean Architecture contract
  and makes the structure the rubric's testable data-structure showcase.
- **Lazy expiry over a sweeper thread.** A background sweeper adds concurrency
  surface and lifecycle management for a `[SHOULD]` optimisation. Lazy eviction
  reclaims memory on the next access to an expired key and is O(1) (YAGNI).

Alternatives considered:

- **Recompute on every read (no cache).** Pros: always fresh, zero staleness,
  no new code. Cons: repeats the full-series read and rate computation on every
  dashboard load, which NFR-P-5 explicitly asks to avoid. Rejected — but its
  simplicity is why the TTL is short and invalidation is explicit, so the cache
  never drifts far from a recompute.
- **External cache (Redis / Memcached).** Pros: shared across worker processes,
  survives restarts, richer eviction. Cons: a new infrastructure dependency,
  network round-trip, and operational burden disproportionate to a single
  cached read model in a course artifact; cloud/infra tooling is explicitly out
  of scope for this milestone. Rejected as premature (YAGNI). The in-process
  cache is the right size now; the `TTLCache` interface (`get`/`set`/
  `invalidate`) is small enough to swap for a Redis-backed adapter later.
- **`functools.lru_cache` / `cachetools.TTLCache`.** Pros: no hand-written
  structure. Cons: `lru_cache` has no TTL and no targeted per-key invalidation;
  pulling in `cachetools` adds a dependency for ~40 lines of code and forfeits
  the rubric's hand-rolled data-structure evidence. Rejected.

DRY/SOLID: the cache is a single generic structure reused via two thin
accessors; the dashboard read-through and the weight-router invalidation each
touch it through a named function rather than a private attribute.

## Consequences

- **Positive**: O(1) cache operations; staleness bounded to ≤ 30 s; a pure,
  fully unit-tested structure with documented complexity; no new runtime
  dependency; Clean Architecture isolation preserved (verified by the
  import-linter architecture test).
- **Negative**:
  - The cache is **per worker process** — it is not shared across uvicorn
    workers, so each worker warms its own copy and a write handled by one worker
    does not invalidate another worker's copy. Bounded by the TTL (≤ 30 s).
  - **Only weight-entry create is an invalidation trigger.** Editing or deleting
    a weight entry does **not** currently bust the cache, so a dashboard read in
    the ≤ 30 s window after an update or delete may be stale. This is an accepted
    limitation for a `[SHOULD]` stretch optimisation; it is bounded by the TTL.
- **Follow-ups**:
  - Add update and delete of a weight entry as invalidation triggers (same
    `invalidate_dashboard_cache` call at those router edges).
  - If multi-worker deployment becomes real, replace the in-process `TTLCache`
    with a shared-cache adapter behind the same `get`/`set`/`invalidate`
    interface (the swap point the interface was designed to allow).

## Alternatives Considered

- **Recompute on read (no cache)** — simplest and always fresh, but repeats the
  expensive full-series read NFR-P-5 asks to avoid; rejected.
- **External cache (Redis/Memcached)** — shared and durable, but a
  disproportionate infrastructure dependency for one cached read model and
  out of scope for this milestone; rejected as premature.
- **`functools.lru_cache` / `cachetools`** — no TTL with targeted invalidation
  (`lru_cache`) or an added dependency for a tiny structure (`cachetools`), and
  forfeits the hand-rolled data-structure evidence; rejected.
