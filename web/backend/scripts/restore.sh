#!/usr/bin/env bash
#
# restore.sh — restore a Weigh to Go! database from a pg_dump custom-format file.
#
# Usage: restore.sh DUMP_FILE
#
# Requires DATABASE_URL. DESTRUCTIVE: drops and recreates objects
# (--clean --if-exists). See docs/runbooks/backup-restore.md.
set -euo pipefail

usage() {
  cat <<'EOF'
Usage: restore.sh DUMP_FILE
  Restore the database in DATABASE_URL from a pg_dump custom-format DUMP_FILE.
  DESTRUCTIVE: overwrites existing objects (--clean --if-exists).
EOF
}

main() {
  if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
    usage
    return 0
  fi
  if [[ $# -ne 1 ]]; then
    usage >&2
    return 2
  fi
  if [[ -z "${DATABASE_URL:-}" ]]; then
    echo "error: DATABASE_URL is not set" >&2
    return 1
  fi

  local dump_file="$1"
  if [[ ! -f "${dump_file}" ]]; then
    echo "error: dump file not found: ${dump_file}" >&2
    return 1
  fi

  local dsn="${DATABASE_URL/postgresql+psycopg:/postgresql:}"
  pg_restore --clean --if-exists --no-owner --no-privileges \
    --dbname="${dsn}" "${dump_file}"
  echo "restore complete from ${dump_file}"
}

main "$@"
