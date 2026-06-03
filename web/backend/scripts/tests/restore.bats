#!/usr/bin/env bats

setup() {
  SCRIPT="${BATS_TEST_DIRNAME}/../restore.sh"
  FAKE_BIN="${BATS_TEST_DIRNAME}/helpers/fake-bin"
  PATH="${FAKE_BIN}:${PATH}"
  WORKDIR="$(mktemp -d)"
  cd "${WORKDIR}"
}

teardown() {
  rm -rf "${WORKDIR}"
}

@test "restore.sh --help prints usage and exits 0" {
  run "${SCRIPT}" --help
  [ "${status}" -eq 0 ]
  [[ "${output}" == *"Usage: restore.sh"* ]]
}

@test "restore.sh fails with usage when no dump file is given" {
  export DATABASE_URL="postgresql://u:p@localhost:5432/db"
  run "${SCRIPT}"
  [ "${status}" -eq 2 ]
  [[ "${output}" == *"Usage: restore.sh"* ]]
}

@test "restore.sh fails when DATABASE_URL is unset" {
  unset DATABASE_URL
  touch in.dump
  run "${SCRIPT}" in.dump
  [ "${status}" -ne 0 ]
  [[ "${output}" == *"DATABASE_URL is not set"* ]]
}

@test "restore.sh fails when the dump file does not exist" {
  export DATABASE_URL="postgresql://u:p@localhost:5432/db"
  run "${SCRIPT}" missing.dump
  [ "${status}" -ne 0 ]
  [[ "${output}" == *"dump file not found"* ]]
}

@test "restore.sh invokes pg_restore for an existing dump file" {
  export DATABASE_URL="postgresql+psycopg://u:p@localhost:5432/db"
  touch in.dump
  run "${SCRIPT}" in.dump
  [ "${status}" -eq 0 ]
  [[ "${output}" == *"FAKE pg_restore"* ]]
  [[ "${output}" == *"postgresql://u:p@localhost:5432/db"* ]]
}

@test "restore.sh fails and prints no success line when pg_restore fails" {
  export DATABASE_URL="postgresql://u:p@localhost:5432/db"
  touch in.dump
  mkdir -p failbin
  printf '#!/usr/bin/env bash\nexit 1\n' >failbin/pg_restore
  chmod +x failbin/pg_restore
  PATH="${PWD}/failbin:${PATH}"
  run "${SCRIPT}" in.dump
  [ "${status}" -ne 0 ]
  [[ "${output}" != *"restore complete"* ]]
}
