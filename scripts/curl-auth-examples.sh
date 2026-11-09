#!/usr/bin/env bash

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"

cat <<EOF
Register:
curl --request POST '${BASE_URL}/api/register' \\
  --header 'Content-Type: application/json' \\
  --data-raw '{"name":"Dung Nguyen","email":"dung@example.com","password":"Testing@12345"}'

Login and save the session cookie. Copy the csrf value from the JSON response:
curl --request POST '${BASE_URL}/api/login' \\
  --cookie-jar cams-cookies.txt \\
  --header 'Content-Type: application/json' \\
  --data-raw '{"email":"dung@example.com","password":"Testing@12345"}'

Session:
curl --cookie cams-cookies.txt '${BASE_URL}/api/session'

Logout:
curl --request POST '${BASE_URL}/api/logout' \\
  --cookie cams-cookies.txt \\
  --header 'X-CSRF-Token: <csrf>'
EOF
