#!/usr/bin/env bats

setup() {
  SCRIPT="${BATS_TEST_DIRNAME}/../backup.sh"
  FAKE_BIN="${BATS_TEST_DIRNAME}/helpers/fake-bin"
  PATH="${FAKE_BIN}:${PATH}"
  WORKDIR="$(mktemp -d)"
  cd "${WORKDIR}"
}

teardown() {
  rm -rf "${WORKDIR}"
}

@test "backup.sh --help prints usage and exits 0" {
  run "${SCRIPT}" --help
  [ "${status}" -eq 0 ]
  [[ "${output}" == *"Usage: backup.sh"* ]]
}

@test "backup.sh fails when DATABASE_URL is unset" {
  unset DATABASE_URL
  run "${SCRIPT}" out.dump
  [ "${status}" -ne 0 ]
  [[ "${output}" == *"DATABASE_URL is not set"* ]]
}

@test "backup.sh invokes pg_dump and writes the named output file" {
  export DATABASE_URL="postgresql+psycopg://u:p@localhost:5432/db"
  run "${SCRIPT}" out.dump
  [ "${status}" -eq 0 ]
  [ -f "out.dump" ]
  [[ "${output}" == *"FAKE pg_dump"* ]]
  [[ "${output}" == *"--file=out.dump"* ]]
  # the SQLAlchemy +psycopg suffix must be stripped for libpq tools
  [[ "${output}" == *"postgresql://u:p@localhost:5432/db"* ]]
}

@test "backup.sh fails and prints no success line when pg_dump fails" {
  export DATABASE_URL="postgresql://u:p@localhost:5432/db"
  mkdir -p failbin
  printf '#!/usr/bin/env bash\nexit 1\n' >failbin/pg_dump
  chmod +x failbin/pg_dump
  PATH="${PWD}/failbin:${PATH}"
  run "${SCRIPT}" out.dump
  [ "${status}" -ne 0 ]
  [[ "${output}" != *"backup written"* ]]
}
