#!/usr/bin/env bash
#
# backup.sh — create a compressed logical backup of the Weigh to Go! database.
#
# Usage: backup.sh [OUTPUT_FILE]
#   OUTPUT_FILE  Destination dump path
#                (default: backups/weighttogo-<UTC timestamp>.dump)
#
# Requires DATABASE_URL in the environment. Documented, not automated —
# see docs/runbooks/backup-restore.md.
set -euo pipefail

usage() {
  cat <<'EOF'
Usage: backup.sh [OUTPUT_FILE]
  Create a compressed pg_dump (custom format) backup of the database in
  DATABASE_URL. OUTPUT_FILE defaults to backups/weighttogo-<timestamp>.dump.
EOF
}

main() {
  if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
    usage
    return 0
  fi
  if [[ -z "${DATABASE_URL:-}" ]]; then
    echo "error: DATABASE_URL is not set" >&2
    return 1
  fi

  # libpq tools (pg_dump) do not understand the SQLAlchemy "+psycopg" driver
  # suffix; strip it to a plain postgresql:// DSN.
  local dsn="${DATABASE_URL/postgresql+psycopg:/postgresql:}"
  local output="${1:-backups/weighttogo-$(date -u +%Y%m%dT%H%M%SZ).dump}"

  mkdir -p "$(dirname "${output}")"
  pg_dump --format=custom --no-owner --no-privileges \
    --dbname="${dsn}" --file="${output}"
  echo "backup written to ${output}"
}

main "$@"
