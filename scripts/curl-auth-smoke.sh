#!/usr/bin/env bash

set -euo pipefail

for command in curl jq; do
  if ! command -v "$command" >/dev/null 2>&1; then
    echo "$command is required." >&2
    exit 1
  fi
done

BASE_URL="${BASE_URL:-http://localhost:8080}"
TEST_NAME="${TEST_NAME:-Dung Nguyen}"
TEST_EMAIL="${TEST_EMAIL:-dung.$(date +%s)@example.com}"
TEST_PASSWORD="${TEST_PASSWORD:-Testing@12345}"
COOKIE_JAR="$(mktemp)"
trap 'rm -f "$COOKIE_JAR"' EXIT

credentials="$(jq -nc --arg email "$TEST_EMAIL" --arg password "$TEST_PASSWORD" '{email:$email,password:$password}')"
registration="$(jq -c --arg name "$TEST_NAME" '. + {name:$name}' <<< "$credentials")"

echo "1. Register"
curl -fsS "$BASE_URL/api/register" \
  --header 'Content-Type: application/json' --data "$registration" | jq -e '.id != null'

echo "2. Log in and save the session cookie"
login="$(curl -fsS "$BASE_URL/api/login" --cookie-jar "$COOKIE_JAR" \
  --header 'Content-Type: application/json' --data "$credentials")"
csrf="$(jq -er '.csrf | select(type == "string" and length > 0)' <<< "$login")"

echo "3. Verify the active session"
curl -fsS "$BASE_URL/api/session" --cookie "$COOKIE_JAR" \
  | jq -e --arg email "$TEST_EMAIL" '.user.email == ($email | ascii_downcase)'

echo "4. Log out"
curl -fsS "$BASE_URL/api/logout" --request POST --cookie "$COOKIE_JAR" \
  --header "X-CSRF-Token: $csrf" | jq -e '.ok == true'

echo "5. Verify the old session is rejected"
status="$(curl -sS "$BASE_URL/api/session" --cookie "$COOKIE_JAR" --output /dev/null --write-out '%{http_code}')"
if [[ "$status" != '401' ]]; then
  echo "Expected 401 after logout, received $status." >&2
  exit 1
fi
