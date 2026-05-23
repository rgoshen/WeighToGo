# ADR-0015: Opaque Compound Cursor for Weight-Entry Pagination

- **Date**: 2026-05-23
- **Status**: Accepted

## Context

The `GET /api/v1/weight-entries` endpoint (FR-W-2) returns the authenticated
user's weight log in pages. The list is ordered `(observation_date DESC,
entry_id DESC)` so a user scrolling backward through their history sees
newest measurements first and entry_id breaks ties when two measurements
share a date.

PR #30 code review surfaced two correctness defects in the first-cut
pagination contract:

1. **Off-by-one skip on the page boundary.** `ListWeightEntries` set
   `next_cursor = rows[command.limit].entry_id` (the *peek* row's ID), and
   the repository filter applied strict `entry_id < before_id`. The peek
   row was therefore excluded on the next page and never returned.

2. **Sort key / cursor key mismatch.** The sort key was `(observation_date,
   entry_id)`, but the cursor encoded `entry_id` only. The two orderings
   diverge whenever a user backfills an older observation date — an entry
   with a higher `entry_id` can sort *after* one with a lower `entry_id`
   when the older date wins the lex compare. With an `entry_id`-only
   cursor the next page filter cannot reproduce the sort order, so rows
   can be repeated, skipped, or both. Concrete reproduction:

   ```
   Insert (today, id=1); insert backfill (2026-01-01, id=2)
   Sort order: [(today, id=1), (2026-01-01, id=2)]
   Page 1 (size=1) returns [(today, id=1)], peek=(2026-01-01, id=2),
     next_cursor = 2.
   Page 2 filter entry_id < 2 returns only [(today, id=1)] — id=2 is
     omitted, id=1 is returned twice.
   ```

The original `ListWeightEntries` docstring already anticipated change: "The
M3 cursor encoding will replace the raw ID with an opaque string without
breaking this shape." This ADR records that change at M2 instead of M3.

## Decision

Replace the `int` `next_cursor` with an **opaque base64-urlsafe string
that encodes the compound sort key** `(observation_date, entry_id)`. The
codec lives in the interface layer (`weight_tracking/interface/cursor.py`),
not the application layer, so the use case can remain in domain terms
(`tuple[date, int] | None`) and the wire format is a pure HTTP concern.

Implementation rules:

- **Wire format**: `base64url(no padding)` of the ASCII string
  `"YYYY-MM-DD:N"`. Example: `MjAyNi0wMS0wMToyMQ` decodes to
  `2026-01-01:21`. Opaque to clients — they round-trip whatever the
  server returned.
- **Use case command**: `cursor: tuple[date, int] | None`. Router decodes
  the string before invoking, encodes the result before responding.
- **Repository port**: `list_for_user(user_id, limit, before: tuple[date,
  int] | None)`. The signature names the compound key explicitly.
- **Repository SQL filter**: when `before` is present, apply
  `(observation_date, entry_id) < (before.date, before.id)`
  lexicographically — i.e. SQLAlchemy
  `tuple_(observation_date, entry_id) < tuple_(before_date, before_id)`,
  or the equivalent disjunction
  `observation_date < before_date OR (observation_date = before_date AND
  entry_id < before_id)`.
- **`next_cursor` derivation**: from the **last returned item**'s
  `(observation_date, entry_id)` with strict `<` on the next page — the
  standard keyset pagination convention. This eliminates the off-by-one
  skip because the boundary row is now the one being returned, not the
  one being peeked.
- **Schema**: `WeightEntryListResponse.next_cursor: str | None`.
- **Frontend type**: `WeightEntryListResponse.next_cursor: string | null`.
- **Error handling**: malformed cursors (bad base64, missing colon, bad
  date) return RFC 7807 422 from the router edge, never reach the use
  case.

## Rationale

The reviewer's two defects share a root cause: the cursor did not encode
the full sort key. The minimum fix that addresses both is a cursor that
carries `(observation_date, entry_id)`. Three encodings were considered:

1. **Plain string `"YYYY-MM-DD:N"`** — readable, no decode step, but
   exposes the format and invites clients to construct cursors by hand,
   which couples them to internals we may later change.
2. **Base64 of `"YYYY-MM-DD:N"`** — opaque, trivial to encode/decode,
   the conventional REST pagination shape, and lets us evolve the
   encoded payload (e.g. add a tiebreaker) without breaking clients
   that round-trip the token.
3. **Two separate query parameters (`cursor_date`, `cursor_id`)** —
   most explicit, but breaks the single-opaque-token contract used
   by REST pagination libraries and complicates the response envelope.

(2) wins for opacity + future evolvability at negligible cost.

Two alternatives addressed the off-by-one alone without compound encoding:

- **Keep `int` cursor, use last-returned ID with strict `<`.** Fixes
  the skip but does nothing for the sort-mismatch case. Reviewer's
  comment explicitly rejected this path.
- **Change sort to `entry_id DESC` only.** Aligns sort with cursor but
  destroys the UX-correct ordering — a backfilled measurement from six
  months ago would appear at the top of the user's log because it has
  the newest entry_id. Wrong tradeoff.

The compound-cursor approach is the only one that satisfies both
reviewer concerns at once.

## Consequences

- **Positive**:
  - Pagination is correct under backfills, concurrent inserts, and
    tie-broken-by-id rows. The page-boundary row is no longer skipped.
  - The wire format is opaque, so future cursor changes (additional
    sort keys, signed tokens) do not require another API version.
  - The use case stays in domain terms — encoding lives at the edge,
    aligned with the three-pattern architecture in ADR-0012.
  - Repository signature now explicitly names what it filters on,
    which makes the SQL clause's intent obvious at the call site.
- **Negative**:
  - **Breaking wire change**: `WeightEntryListResponse.next_cursor`
    type flips from `integer | null` to `string | null`. The frontend
    type, OpenAPI snapshot, and any Playwright spec that inspected
    the cursor must be updated in the same PR. Acceptable here
    because no consumers exist outside this repository.
  - The repository port signature changes from `before_id: int | None`
    to `before: tuple[date, int] | None`. There is one implementation
    and the in-tree mocks are easy to update.
  - Slightly more code at the edge (codec module + decode-on-request,
    encode-on-response). Tested in isolation as a pure value-object.
- **Follow-ups**:
  - Apply the same cursor convention to any future paginated list in
    the weight or auth slices (goals, sessions) so the contract is
    uniform across resources.
  - Consider signing/HMAC-ing the cursor in M3 if cursors begin to
    encode information clients should not be able to forge (e.g.
    per-user partition keys).

## Alternatives Considered

- **Keep `int` cursor, fix the off-by-one only by using the
  last-returned `entry_id` with strict `<`.** Rejected — does not
  address the sort-mismatch defect the reviewer separately flagged.
  The bug would still reproduce under any backfilled-date workload.
- **Keep `int` cursor; have the repository look up the cursor entry's
  `observation_date` server-side before filtering.** Rejected — adds
  a `SELECT observation_date FROM weight_entries WHERE entry_id = ?`
  on every paginated request and hides a non-trivial sort-key
  dependency inside the adapter. Worse separation than encoding the
  key explicitly in the cursor.
- **Sort by `entry_id DESC` only.** Rejected — aligns sort with the
  existing `int` cursor but produces a creation-time-ordered log
  instead of an observation-date-ordered log, which is a UX
  regression for a weight tracker.
- **Plain (non-base64) compound cursor.** Rejected — exposes the
  format and encourages client-side construction, which would later
  block any change to the encoded payload.
