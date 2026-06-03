# Weigh to Go! — Migration History

This table lists every Alembic migration in the chain (`0001`–`0010`), its
purpose, the milestone that introduced it, and the key schema objects it
creates or modifies.

Migrations `0001`–`0008` were authored during Milestones 2 and 3.  Milestone 4
(GH-97, GH-98, GH-99) added migrations `0009`–`0010` and audited the full
chain for reversibility (upgrade → downgrade → upgrade round-trip verified in
CI — see `.github/workflows/migration-ci.yml`).

| Migration | Purpose | Milestone | Key objects |
|-----------|---------|-----------|-------------|
| `0001` | Initial users + auth (CITEXT email, refresh tokens) | M2 | `users`, `refresh_tokens` |
| `0002` | Weight entries + composite performance indexes | M2 | `weight_entries`, `idx_weight_entries_user_date_active`, `idx_weight_entries_user_observation_desc` |
| `0003` | Goals table | M3 | `goals` |
| `0004` | Goals direction-invariant CHECK constraint | M3 | `goals_direction_invariant` |
| `0005` | Achievements table + indexes | M3 | `achievements` |
| `0006` | User preferences table (EAV storage, ADR-0020) | M3 | `user_preferences` |
| `0007` | Composite `created_at` index for NFR-P-3 (ADR-0021) | M3 | `idx_weight_entries_user_created_at` |
| `0008` | Widen `achievement_type` CHECK to include `'streak'` | M3 | `achievements_type_valid` |
| `0009` | Audit log table + indexes (ADR-0024) | M4 | `audit_log`, `idx_audit_log_user_created`, `idx_audit_log_event_type_created` |
| `0010` | Constraint hardening + goals listing index (ADR-0025) | M4 | `achievements_threshold_positive`, `goals_target_date_epoch`, `idx_goals_user_created` |
